package name.julatec.ekonomi.chat;

import name.julatec.descubrir.Destino;
import name.julatec.descubrir.NadieConectaException;
import name.julatec.descubrir.Recorrido;
import name.julatec.descubrir.Resolutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A dónde se le habla al modelo.
 * <p>
 * {@code ekonomi.chat.base-url} es una URL normal —y entonces todo se comporta como siempre— o
 * {@code srv:<capacidad>}: la capacidad se busca por DNS-SD (registros SRV) en el dominio de la casa y el destino
 * sale del registro, no del código. Así Ekonomi deja de llevar escrita la IP del servidor de modelos, y un cambio
 * de máquina, de IP o de puerto se hace en el DNS sin tocar a nadie.
 * <p>
 * <b>Es la única clase de Ekonomi que toca {@code name.julatec.descubrir}.</b> A propósito: si la biblioteca
 * cambia de nombre, el cambio es de un archivo.
 *
 * <h2>Cómo se elige</h2>
 * <ul>
 *   <li><b>Por petición</b>, no una vez al arrancar: un preferido caído deja de intentarse apenas cae, y apenas vuelve
 *   recupera su tráfico. El {@link Resolutor} cachea el registro (5 a 300 s) y, si el DNS falla, sirve el vencido.</li>
 *   <li><b>{@code Conectar} se usa como selector, no como conexión.</b> Devuelve un socket ya conectado y
 *   {@code HttpClient} no puede usarlo, así que se valida al ganador, se cierra, y se arma la URL con su
 *   {@code host:puerto}. Cuesta un apretón TCP de más (unos 17 ms en la LAN).</li>
 *   <li><b>El esquema es {@code http}.</b> Un SRV no lo lleva, y los modelos de la casa hablan HTTP plano en la LAN.</li>
 * </ul>
 *
 * <h2>Lo que NO hace</h2>
 * <ul>
 *   <li><b>No detecta un servidor colgado:</b> la carrera es solo de TCP, y un {@code llama-server} que acepta la
 *   conexión y no contesta se elige igual. Para eso ya está el tiempo de respuesta de {@link ClienteModelo}; las
 *   sondas de capa 7 serían lo siguiente.</li>
 *   <li><b>No es failover real todavía:</b> hoy cada capacidad de modelos tiene un solo destino.</li>
 * </ul>
 *
 * <h2>Respaldo</h2>
 * {@code ekonomi.chat.base-url-respaldo}, una URL fija (con IP literal, o no sirve cuando el DNS es lo que falla).
 * Se usa solo si el descubrimiento no pudo elegir a nadie, y siempre con un {@code WARN}: un respaldo silencioso
 * convierte una falla del DNS en algo que nadie ve.
 */
@Component
public class UbicacionDelModelo {

    private static final Logger logger = LoggerFactory.getLogger(UbicacionDelModelo.class);

    static final String PREFIJO_SRV = "srv:";

    /**
     * Cuánto se espera a que alguien conecte. El mismo plazo corto que ya tenía {@code HttpClient}: si el modelo
     * está apagado se sabe en cinco segundos, no en dos minutos.
     */
    static final Duration PLAZO_DE_ELECCION = Duration.ofSeconds(5);

    private final String configurada;
    private final String respaldo;
    private final Resolutor resolutor;
    private final Duration plazo;

    /**
     * Un solo {@link Resolutor} por proceso (la guía común lo pide): es el que lleva la caché.
     * El valor por omisión es el de siempre —la IP del servidor y el puerto de siempre— para que desplegar este código
     * no cambie nada hasta que alguien ponga {@code srv:<capacidad>} en {@code catalina.properties}.
     */
    @Autowired
    public UbicacionDelModelo(
            @Value("${ekonomi.chat.base-url:http://172.16.13.117:8080}") String configurada,
            @Value("${ekonomi.chat.base-url-respaldo:}") String respaldo) {
        this(configurada, respaldo, new Resolutor(), PLAZO_DE_ELECCION);
    }

    /** Para las pruebas: un {@link Resolutor} con un buscador falso, sin DNS. */
    UbicacionDelModelo(String configurada, String respaldo, Resolutor resolutor, Duration plazo) {
        this.configurada = limpiar(configurada);
        this.respaldo = limpiar(respaldo);
        this.resolutor = resolutor;
        this.plazo = plazo;
        if (this.configurada.startsWith(PREFIJO_SRV) && capacidad().isBlank()) {
            // Mejor que arranque fallando a que el chat falle la primera vez que alguien pregunte.
            throw new IllegalArgumentException("ekonomi.chat.base-url=\"" + configurada
                    + "\" necesita una capacidad después de `srv:` (por ejemplo `srv:llama`).");
        }
    }

    /** Lo configurado, tal cual: {@code srv:llama} o la URL. Es lo que muestra el estado del chat. */
    public String descripcion() {
        return configurada;
    }

    /**
     * La URL base para esta petición, sin barra final.
     *
     * @throws Inalcanzable si es {@code srv:} y no se pudo elegir a nadie ni hay respaldo.
     */
    public String baseUrl() throws Inalcanzable {
        if (!configurada.startsWith(PREFIJO_SRV)) {
            return configurada;
        }
        final String causa;
        try {
            final List<Destino> destinos = resolutor.buscar(capacidad(), "tcp");
            final Recorrido.Conexion ganadora = Recorrido.conectar(destinos, Recorrido.ESCALON_POR_OMISION, plazo);
            cerrar(ganadora.socket());
            final Destino destino = ganadora.destino();
            logger.debug("{} -> {}", configurada, destino);
            return "http://" + destino.host() + ":" + destino.puerto();
        } catch (NadieConectaException e) {
            causa = describir(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Inalcanzable(configurada, "se interrumpió la búsqueda");
        } catch (Exception e) {
            // NoHayServicio, ServicioDeclaradoAusente, el NamingException del DNS: la biblioteca declara `Exception`.
            causa = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }
        if (!respaldo.isEmpty()) {
            logger.warn("{} no se pudo resolver ({}); se usa el respaldo {}", configurada, causa, respaldo);
            return respaldo;
        }
        throw new Inalcanzable(configurada, causa);
    }

    private String capacidad() {
        return configurada.substring(PREFIJO_SRV.length()).trim();
    }

    private static String limpiar(String url) {
        final String t = url == null ? "" : url.trim();
        return t.endsWith("/") ? t.substring(0, t.length() - 1) : t;
    }

    private static void cerrar(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            logger.debug("no se pudo cerrar el socket de la elección", e);
        }
    }

    /**
     * Una causa por candidato, en el orden de la lista —que es lo que la especificación pide—, en palabras y no en la
     * letra F/R/T/N del protocolo: esto lo lee quien está usando el chat.
     */
    private static String describir(NadieConectaException e) {
        return e.fallos().stream()
                .map(f -> f.destino() + " " + switch (f.clase()) {
                    case FALLO -> "falló" + (f.causa() == null ? "" : " (" + f.causa().getMessage() + ")");
                    case RECHAZADO -> "fue rechazado";
                    case SIN_RESPUESTA -> "no respondió a tiempo";
                    case NO_INTENTADO -> "no se llegó a intentar";
                })
                .collect(Collectors.joining("; "));
    }

    /** No se pudo elegir ningún destino. El mensaje ya viene listo para quien usa el chat. */
    public static final class Inalcanzable extends IOException {
        private static final long serialVersionUID = 1L;

        Inalcanzable(String configurada, String causa) {
            super("No se pudo conectar con el modelo (" + configurada + "): " + causa + ".");
        }
    }
}

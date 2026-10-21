package name.julatec.ekonomi.chat;

import name.julatec.descubrir.NoHayServicioException;
import name.julatec.descubrir.Registro;
import name.julatec.descubrir.Resolutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Con sockets de verdad —uno abierto y otro cerrado— y no con dobles de la conexión: lo que hay que
 * probar es que la carrera de {@code Recorrido} decide bien, y eso solo se ve con un puerto que
 * rechaza y otro que acepta. El DNS sí es falso: el {@link Resolutor} recibe un buscador que devuelve
 * lo que la prueba diga, sin red.
 */
class UbicacionDelModeloTest {

    private static final Duration PLAZO = Duration.ofSeconds(2);

    private static Resolutor resolutorCon(List<Registro> registros, List<String> nombresPedidos) {
        return new Resolutor(nombre -> {
            nombresPedidos.add(nombre);
            return new Resolutor.Respuesta(registros, Duration.ofSeconds(60));
        }, () -> 0.5, aviso -> { });
    }

    private static Resolutor resolutorCon(List<Registro> registros) {
        return resolutorCon(registros, new ArrayList<>());
    }

    private static Resolutor resolutorQueNoSePuedeUsar() {
        return new Resolutor(nombre -> {
            throw new AssertionError("no debía consultar el DNS: " + nombre);
        }, () -> 0.5, aviso -> { });
    }

    private static Resolutor resolutorSinRegistro() {
        return new Resolutor(nombre -> {
            throw new NoHayServicioException(nombre);
        }, () -> 0.5, aviso -> { });
    }

    /** Un puerto en el que seguro nadie escucha: se reserva y se suelta. */
    private static int puertoCerrado() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    private static Registro en(int prioridad, int puerto) {
        // El punto final es como llega un nombre de un registro SRV.
        return new Registro(prioridad, 0, puerto, "localhost.");
    }

    private static UbicacionDelModelo srv(String capacidad, String respaldo, Resolutor resolutor) {
        return new UbicacionDelModelo("srv:" + capacidad, respaldo, resolutor, PLAZO);
    }

    @Test
    @DisplayName("una URL normal no consulta el DNS, y pierde la barra final")
    void unaUrlNormalNoConsultaElDns() throws Exception {
        final UbicacionDelModelo u = new UbicacionDelModelo("http://modelo.example:8080/", "", resolutorQueNoSePuedeUsar(), PLAZO);
        assertEquals("http://modelo.example:8080", u.baseUrl());
        assertEquals("http://modelo.example:8080", u.descripcion());
    }

    @Test
    @DisplayName("srv: devuelve la URL del destino que conecta")
    void srvDevuelveLaUrlDelQueConecta() throws Exception {
        try (ServerSocket abierto = new ServerSocket(0)) {
            final UbicacionDelModelo u = srv("llama", "", resolutorCon(List.of(en(0, abierto.getLocalPort()))));
            assertEquals("http://localhost:" + abierto.getLocalPort(), u.baseUrl());
        }
    }

    @Test
    @DisplayName("si el preferido está caído se pasa al siguiente: es lo que ninguna URL fija puede hacer")
    void siElPreferidoEstaCaidoSaltaAlSiguiente() throws Exception {
        try (ServerSocket respaldo = new ServerSocket(0)) {
            final UbicacionDelModelo u = srv("llama", "", resolutorCon(
                    List.of(en(0, puertoCerrado()), en(10, respaldo.getLocalPort()))));
            assertEquals("http://localhost:" + respaldo.getLocalPort(), u.baseUrl());
        }
    }

    @Test
    @DisplayName("se elige en cada petición: un preferido que cae deja de intentarse, y uno que vuelve recupera su tráfico")
    void seEligeEnCadaPeticion() throws Exception {
        final ServerSocket preferido = new ServerSocket(0);
        try (ServerSocket respaldo = new ServerSocket(0)) {
            final UbicacionDelModelo u = srv("llama", "", resolutorCon(
                    List.of(en(0, preferido.getLocalPort()), en(10, respaldo.getLocalPort()))));

            assertEquals("http://localhost:" + preferido.getLocalPort(), u.baseUrl(), "con los dos arriba, el preferido");
            preferido.close();
            assertEquals("http://localhost:" + respaldo.getLocalPort(), u.baseUrl(),
                    "caído el preferido, la misma lista cacheada manda a la siguiente petición al respaldo");
        } finally {
            preferido.close();
        }
    }

    @Test
    @DisplayName("la capacidad se pide como _<capacidad>._tcp, sin el prefijo srv:")
    void laCapacidadSePideSinElPrefijo() throws Exception {
        final List<String> pedidos = new ArrayList<>();
        try (ServerSocket abierto = new ServerSocket(0)) {
            srv("modelos", "", resolutorCon(List.of(en(0, abierto.getLocalPort())), pedidos)).baseUrl();
        }
        assertEquals(1, pedidos.size());
        assertTrue(pedidos.get(0).startsWith("_modelos._tcp."), "se pidió: " + pedidos.get(0));
    }

    @Test
    @DisplayName("si nadie conecta, el error trae una causa por candidato")
    void nadieConectaDaUnaCausaPorCandidato() throws Exception {
        final int uno = puertoCerrado();
        final int dos = puertoCerrado() == uno ? puertoCerrado() : puertoCerrado();
        final UbicacionDelModelo u = srv("llama", "", resolutorCon(List.of(en(0, uno), en(10, dos))));

        final UbicacionDelModelo.Inalcanzable e = assertThrows(UbicacionDelModelo.Inalcanzable.class, u::baseUrl);

        assertTrue(e.getMessage().startsWith("No se pudo conectar con el modelo (srv:llama): "), e.getMessage());
        assertTrue(e.getMessage().contains("localhost:" + uno), "debe nombrar al primer candidato: " + e.getMessage());
        assertTrue(e.getMessage().contains("localhost:" + dos), "y al segundo: " + e.getMessage());
    }

    @Test
    @DisplayName("si nadie conecta y hay respaldo, se usa el respaldo")
    void nadieConectaPeroHayRespaldo() throws Exception {
        final UbicacionDelModelo u = srv("llama", "http://192.0.2.10:8080/",
                resolutorCon(List.of(en(0, puertoCerrado()))));
        assertEquals("http://192.0.2.10:8080", u.baseUrl());
    }

    @Test
    @DisplayName("sin registro y sin respaldo, el error dice que no hay servicio")
    void sinRegistroSinRespaldo() {
        final UbicacionDelModelo u = srv("llama", "", resolutorSinRegistro());
        final UbicacionDelModelo.Inalcanzable e = assertThrows(UbicacionDelModelo.Inalcanzable.class, u::baseUrl);
        assertTrue(e.getMessage().contains("no hay servicio"), e.getMessage());
    }

    @Test
    @DisplayName("sin registro pero con respaldo, se usa el respaldo")
    void sinRegistroConRespaldo() throws Exception {
        final UbicacionDelModelo u = srv("llama", "http://192.0.2.10:8080", resolutorSinRegistro());
        assertEquals("http://192.0.2.10:8080", u.baseUrl());
    }

    @Test
    @DisplayName("`srv:` sin capacidad falla al arrancar, no la primera vez que alguien pregunta")
    void srvSinCapacidadFallaAlArrancar() {
        assertThrows(IllegalArgumentException.class,
                () -> new UbicacionDelModelo("srv:  ", "", resolutorQueNoSePuedeUsar(), PLAZO));
    }
}

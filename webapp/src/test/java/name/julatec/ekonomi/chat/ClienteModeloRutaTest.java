package name.julatec.ekonomi.chat;

import com.sun.net.httpserver.HttpServer;
import name.julatec.descubrir.Registro;
import name.julatec.descubrir.Resolutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * La ruta que usa producción, de punta a punta: {@code ClienteModelo} → {@link UbicacionDelModelo} →
 * {@code HttpClient} → un servidor HTTP de verdad. Las pruebas de {@link UbicacionDelModeloTest} fijan la
 * elección; estas fijan que lo elegido es lo que de hecho recibe la petición, con su llave y su cuerpo.
 * <p>
 * Existe por una lección de este mismo día: un arreglo con pruebas que fallaban sin él, puesto en una
 * clase por la que el documento real nunca pasaba. Una prueba que ejercita otra ruta que la de producción
 * no prueba nada.
 */
class ClienteModeloRutaTest {

    private HttpServer servidor;
    private final List<String> peticiones = new CopyOnWriteArrayList<>();
    private final List<String> llaves = new CopyOnWriteArrayList<>();

    @BeforeEach
    void levantarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        servidor.createContext("/v1/chat/completions", intercambio -> {
            peticiones.add(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            llaves.add(String.valueOf(intercambio.getRequestHeaders().getFirst("Authorization")));
            final byte[] cuerpo = ("{\"choices\":[{\"message\":{\"role\":\"assistant\","
                    + "\"content\":\"hola desde el modelo\"}}]}").getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(200, cuerpo.length);
            intercambio.getResponseBody().write(cuerpo);
            intercambio.close();
        });
        servidor.start();
    }

    @AfterEach
    void bajarServidor() {
        servidor.stop(0);
    }

    private int puerto() {
        return servidor.getAddress().getPort();
    }

    private static int puertoCerrado() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    private static ClienteModelo cliente(UbicacionDelModelo ubicacion) {
        final ClienteModelo c = new ClienteModelo(mock(HerramientasDelChat.class), ubicacion);
        // Lo que Spring inyecta con @Value.
        ReflectionTestUtils.setField(c, "modelo", "qwen3vl-8b");
        ReflectionTestUtils.setField(c, "apiKey", "llave-de-prueba");
        ReflectionTestUtils.setField(c, "esperaSegundos", 5);
        return c;
    }

    private static Resolutor resolutorCon(List<Registro> registros) {
        return new Resolutor(nombre -> new Resolutor.Respuesta(registros, Duration.ofSeconds(60)),
                () -> 0.5, aviso -> { });
    }

    @Test
    @DisplayName("con srv:, un preferido caído no impide que la petición llegue al respaldo, con su llave y su cuerpo")
    void conSrvLaPeticionLlegaAlRespaldo() throws Exception {
        final UbicacionDelModelo u = new UbicacionDelModelo("srv:llama", "", resolutorCon(List.of(
                new Registro(0, 0, puertoCerrado(), "localhost."),
                new Registro(10, 0, puerto(), "localhost."))), Duration.ofSeconds(2));

        final ClienteModelo.Respuesta r = cliente(u).conversar("sistema", List.of(), "hola");

        assertTrue(r.ok(), "error: " + r.error());
        assertEquals("hola desde el modelo", r.texto());
        assertEquals(1, peticiones.size(), "tenía que llegar exactamente una petición al servidor del respaldo");
        assertTrue(peticiones.get(0).contains("\"model\":\"qwen3vl-8b\""), peticiones.get(0));
        assertEquals("Bearer llave-de-prueba", llaves.get(0), "la llave tiene que seguir yendo, venga la URL de donde venga");
    }

    @Test
    @DisplayName("una URL normal se comporta igual que antes de que existiera el descubrimiento")
    void unaUrlNormalSeComportaIgual() {
        final UbicacionDelModelo u = new UbicacionDelModelo("http://localhost:" + puerto(), "",
                resolutorCon(List.of()), Duration.ofSeconds(2));

        final ClienteModelo.Respuesta r = cliente(u).conversar("sistema", List.of(), "hola");

        assertTrue(r.ok(), "error: " + r.error());
        assertEquals("hola desde el modelo", r.texto());
        assertEquals(1, peticiones.size());
    }

    @Test
    @DisplayName("si nadie conecta, quien usa el chat recibe la causa de cada candidato y no un cuelgue")
    void siNadieConectaElChatDiceLaCausa() throws Exception {
        final UbicacionDelModelo u = new UbicacionDelModelo("srv:llama", "", resolutorCon(List.of(
                new Registro(0, 0, puertoCerrado(), "localhost."))), Duration.ofSeconds(2));

        final ClienteModelo.Respuesta r = cliente(u).conversar("sistema", List.of(), "hola");

        assertFalse(r.ok());
        assertTrue(r.error().startsWith("No se pudo conectar con el modelo (srv:llama): "), r.error());
        assertTrue(peticiones.isEmpty(), "no debió llegar nada al servidor de la prueba");
    }

    @Test
    @DisplayName("el estado del chat muestra lo configurado, no una IP")
    void elEstadoMuestraLoConfigurado() {
        final UbicacionDelModelo u = new UbicacionDelModelo("srv:llama", "", resolutorCon(List.of()), Duration.ofSeconds(2));
        assertEquals("srv:llama", cliente(u).ubicacion());
    }
}

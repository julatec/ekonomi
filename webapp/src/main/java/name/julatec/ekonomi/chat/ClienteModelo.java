package name.julatec.ekonomi.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Habla con el modelo local de pitia por su API compatible con OpenAI, y resuelve el lazo de
 * herramientas.
 * <p>
 * <b>Sin Spring AI, y es una decisión medida.</b> El starter
 * {@code spring-ai-starter-model-openai} agrega <b>62 artefactos y 78,5 MB</b> a un WAR que ya
 * pesa 89,6 MB y corre en una Raspberry Pi; 50,4 MB de eso es {@code openai-java-core}, que no
 * se puede excluir. Todo lo que hace falta acá es un POST con JSON: {@code HttpClient} viene en
 * el JDK y Jackson ya está en el árbol. Además, los valores por omisión de Spring AI pelean con
 * este modelo —60 s de espera y 3 reintentos— y la plantilla de Ministral tiene una restricción
 * que conviene controlar a mano.
 * <p>
 * 🔴 <b>Esa restricción:</b> la plantilla de Ministral acepta el rol {@code system} <b>solo en
 * la primera posición</b>. Si aparece a mitad de la conversación, el servidor responde HTTP 500.
 * Por eso el mensaje de sistema se arma una vez, va primero, y el historial que llega del
 * navegador se filtra a {@code user} y {@code assistant}.
 */
@Component
public class ClienteModelo {

    private static final Logger logger = LoggerFactory.getLogger(ClienteModelo.class);

    /**
     * Cuántas veces se le deja pedir herramientas antes de exigirle una respuesta.
     * <p>
     * Cuatro y no «las que haga falta»: cada vuelta agrega la pregunta, la llamada y su
     * resultado al contexto, y el de pitia son 16.384 tokens. Un lazo sin tope no se cuelga —se
     * queda sin ventana y empieza a responder cualquier cosa.
     */
    private static final int VUELTAS_MAXIMAS = 4;

    /** Recorte de cada resultado de herramienta antes de mandarlo al modelo. */
    private static final int TOPE_CARACTERES_RESULTADO = 6000;

    private final ObjectMapper json = new ObjectMapper();
    private final HerramientasDelChat herramientas;
    private final HttpClient http;

    @Value("${ekonomi.chat.base-url:http://172.16.13.117:8080}")
    private String baseUrl;

    @Value("${ekonomi.chat.modelo:ministral}")
    private String modelo;

    @Value("${EKONOMI_PITIA_API_KEY:${ekonomi.chat.api-key:}}")
    private String apiKey;

    @Value("${ekonomi.chat.espera-segundos:120}")
    private int esperaSegundos;

    public ClienteModelo(HerramientasDelChat herramientas) {
        this.herramientas = herramientas;
        this.http = HttpClient.newBuilder()
                // Corto a propósito: si pitia está apagada se sabe en cinco segundos, no en dos
                // minutos. La espera larga es la de la respuesta, que sí puede tardar.
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /** Lo que el chat le devuelve a la interfaz. */
    public record Respuesta(boolean ok, String texto, List<String> herramientasUsadas, String error) {
        static Respuesta fallo(String error) {
            return new Respuesta(false, null, List.of(), error);
        }
    }

    public Respuesta conversar(String sistema, List<Map<String, String>> historial, String pregunta) {
        final ArrayNode mensajes = json.createArrayNode();
        mensajes.add(json.createObjectNode().put("role", "system").put("content", sistema));
        for (Map<String, String> m : alternado(historial, pregunta)) {
            mensajes.add(json.createObjectNode().put("role", m.get("rol")).put("content", m.get("texto")));
        }

        final Set<String> usadas = new LinkedHashSet<>();
        try {
            for (int vuelta = 0; vuelta < VUELTAS_MAXIMAS; vuelta++) {
                final JsonNode eleccion = pedir(mensajes, vuelta < VUELTAS_MAXIMAS - 1);
                final JsonNode mensaje = eleccion.path("message");
                final JsonNode llamadas = mensaje.path("tool_calls");

                if (!llamadas.isArray() || llamadas.isEmpty()) {
                    final String texto = mensaje.path("content").asString("").trim();
                    return new Respuesta(true, texto.isEmpty() ? "(el modelo no respondió nada)" : texto,
                            List.copyOf(usadas), null);
                }

                // El mensaje del asistente con sus llamadas tiene que volver tal cual, o el
                // modelo pierde el hilo de qué pidió.
                mensajes.add(mensaje);
                for (JsonNode llamada : llamadas) {
                    final String nombre = llamada.path("function").path("name").asString("");
                    final String crudos = llamada.path("function").path("arguments").asString("{}");
                    usadas.add(nombre);
                    mensajes.add(json.createObjectNode()
                            .put("role", "tool")
                            .put("tool_call_id", llamada.path("id").asString(""))
                            .put("name", nombre)
                            .put("content", ejecutar(nombre, crudos)));
                }
            }
            return new Respuesta(false, null, List.copyOf(usadas),
                    "El modelo pidió herramientas " + VUELTAS_MAXIMAS + " veces seguidas sin llegar a "
                            + "una respuesta. Probá con una pregunta más concreta.");
        } catch (java.net.http.HttpTimeoutException e) {
            return Respuesta.fallo("El modelo no respondió en " + esperaSegundos + " s.");
        } catch (java.net.ConnectException e) {
            return Respuesta.fallo("No se pudo conectar con el modelo en " + baseUrl + ".");
        } catch (Exception e) {
            logger.warn("Falló la conversación con el modelo", e);
            return Respuesta.fallo("Falló la conversación con el modelo: " + e.getMessage());
        }
    }

    /**
     * Normaliza el historial a lo único que la plantilla de Ministral acepta: un {@code system}
     * al principio y después <b>alternancia estricta</b> user/assistant/user/…
     * <p>
     * No es defensa contra un frontend descuidado, es una restricción del modelo que el servidor
     * tiene que hacer cumplir. Dos {@code user} seguidos —que es lo que pasa si alguien manda una
     * segunda pregunta antes de que llegue la respuesta— devuelven <b>HTTP 500</b>:
     * <pre>
     *   Jinja Exception: After the optional system message, conversation roles must alternate…
     * </pre>
     * Reproducido el 5 sep 2026 mandando un historial con dos {@code user} consecutivos.
     * <p>
     * Los consecutivos del mismo rol se funden en uno en vez de descartarse: perder lo que alguien
     * escribió para cumplir una regla de plantilla sería peor que la regla. Y un historial que
     * empiece por {@code assistant} pierde ese primer turno, que no tiene con qué alternar.
     */
    static List<Map<String, String>> alternado(List<Map<String, String>> historial, String pregunta) {
        final List<Map<String, String>> salida = new ArrayList<>();
        for (Map<String, String> m : historial) {
            final String rol = m.get("rol");
            if (!"user".equals(rol) && !"assistant".equals(rol)) {
                continue;  // un `system` a mitad de camino también rompe la plantilla
            }
            final String texto = m.getOrDefault("texto", "");
            if (salida.isEmpty()) {
                if ("assistant".equals(rol)) {
                    continue;
                }
            } else if (rol.equals(salida.get(salida.size() - 1).get("rol"))) {
                final Map<String, String> ultimo = salida.get(salida.size() - 1);
                salida.set(salida.size() - 1,
                        Map.of("rol", rol, "texto", ultimo.get("texto") + "\n\n" + texto));
                continue;
            }
            salida.add(Map.of("rol", rol, "texto", texto));
        }
        // La pregunta nueva es siempre `user`. Si el historial ya terminaba en `user`, se funden.
        if (!salida.isEmpty() && "user".equals(salida.get(salida.size() - 1).get("rol"))) {
            final Map<String, String> ultimo = salida.get(salida.size() - 1);
            salida.set(salida.size() - 1,
                    Map.of("rol", "user", "texto", ultimo.get("texto") + "\n\n" + pregunta));
        } else {
            salida.add(Map.of("rol", "user", "texto", pregunta));
        }
        return salida;
    }

    /** Ejecuta una herramienta y devuelve SIEMPRE texto: un error también es información para el modelo. */
    private String ejecutar(String nombre, String argumentosCrudos) {
        try {
            final JsonNode nodo = json.readTree(argumentosCrudos.isBlank() ? "{}" : argumentosCrudos);
            final Map<String, Object> argumentos = new LinkedHashMap<>();
            nodo.propertyStream().forEach(e -> argumentos.put(e.getKey(), valor(e.getValue())));
            final String salida = herramientas.invocar(nombre, argumentos);
            return salida.length() > TOPE_CARACTERES_RESULTADO
                    ? salida.substring(0, TOPE_CARACTERES_RESULTADO)
                        + "\n…recortado. Acotá el filtro o pedí menos filas."
                    : salida;
        } catch (Exception e) {
            // Devolver el error como resultado, en vez de propagarlo, deja que el modelo se
            // corrija solo —típicamente pidiendo el tenant que faltaba— en la vuelta siguiente.
            return "{\"error\":" + json.writeValueAsString(String.valueOf(e.getMessage())) + "}";
        }
    }

    private static Object valor(JsonNode n) {
        if (n.isNumber()) return n.numberValue();
        if (n.isBoolean()) return n.booleanValue();
        if (n.isNull()) return null;
        return n.isValueNode() ? n.asString("") : n.toString();
    }

    private JsonNode pedir(ArrayNode mensajes, boolean conHerramientas) throws Exception {
        final ObjectNode cuerpo = json.createObjectNode();
        cuerpo.put("model", modelo);
        cuerpo.set("messages", mensajes);
        cuerpo.put("temperature", 0.1);
        if (conHerramientas) {
            cuerpo.set("tools", json.valueToTree(herramientas.catalogo()));
            cuerpo.put("tool_choice", "auto");
        }

        final HttpRequest.Builder peticion = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/chat/completions"))
                .timeout(Duration.ofSeconds(esperaSegundos))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(cuerpo)));
        if (apiKey != null && !apiKey.isBlank()) {
            peticion.header("Authorization", "Bearer " + apiKey);
        }

        final HttpResponse<String> respuesta = http.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
        if (respuesta.statusCode() != 200) {
            throw new IllegalStateException("el modelo respondió HTTP " + respuesta.statusCode()
                    + ": " + recorte(respuesta.body()));
        }
        final JsonNode elecciones = json.readTree(respuesta.body()).path("choices");
        if (!elecciones.isArray() || elecciones.isEmpty()) {
            throw new IllegalStateException("el modelo respondió sin `choices`");
        }
        return elecciones.get(0);
    }

    private static String recorte(String s) {
        return s == null ? "" : s.length() > 300 ? s.substring(0, 300) + "…" : s;
    }

    public List<String> nombresDeHerramientas() {
        return new ArrayList<>(herramientas.catalogo().stream()
                .map(t -> String.valueOf(((Map<?, ?>) t.get("function")).get("name"))).toList());
    }
}

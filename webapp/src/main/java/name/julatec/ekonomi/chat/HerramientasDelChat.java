package name.julatec.ekonomi.chat;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import name.julatec.ekonomi.mcp.EkonomiMcpTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.spring.SyncMcpAnnotationProviders;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Las mismas herramientas del servidor MCP, puestas a disposición del chat.
 * <p>
 * No hay un segundo catálogo ni una segunda implementación: {@code SyncMcpToolProvider} lee los
 * métodos anotados con {@code @McpTool} de {@link EkonomiMcpTools} y devuelve, por cada uno, su
 * nombre, su descripción y el JSON Schema de sus parámetros —ya generado, con los nombres reales
 * y no {@code arg0}—. Si mañana se agrega una herramienta al MCP, el chat la ve sola.
 * <p>
 * <b>Se ejecutan en el mismo proceso y en el mismo hilo</b>, así que el {@code SecurityContext}
 * del usuario llega intacto adentro de la herramienta: el modelo nunca toca {@code /mcp} ni
 * necesita certificado, y {@code AccesoTenant} sigue validando cada tenant contra
 * {@code user.getDatasources()}. Un modelo que pida una contabilidad ajena recibe un error, no
 * datos.
 */
@Component
public class HerramientasDelChat {

    private static final Logger logger = LoggerFactory.getLogger(HerramientasDelChat.class);

    /**
     * Las que el chat puede usar. Es una lista blanca y no una lista negra a propósito: una
     * herramienta nueva no queda expuesta al modelo hasta que alguien la agregue acá a mano.
     * <p>
     * Quedan afuera, y por razones distintas:
     * <ul>
     *   <li>{@code reporte_compras} y {@code reporte_ventas} devuelven un .xlsx en base64. Un
     *       adjunto de decenas de kilobytes no cabe en los 16.384 tokens de contexto del
     *       modelo local, y aunque cupiera no hay nada que el modelo pueda leer ahí.</li>
     *   <li>{@code consulta_sql} es demasiada superficie para una primera versión: acepta SQL
     *       que redacta el modelo a partir de texto que escribe cualquiera.</li>
     * </ul>
     */
    private static final Set<String> PERMITIDAS = Set.of(
            "listar_tenants",
            "estado_ekonomi",
            "esquema",
            "buscar_comprobantes",
            "detalle_comprobante",
            "resumen_periodo",
            "clientes_frecuentes",
            "consultar_cabys",
            "consultar_actividad");

    private final Map<String, McpServerFeatures.SyncToolSpecification> porNombre;

    public HerramientasDelChat(EkonomiMcpTools herramientas) {
        final Map<String, McpServerFeatures.SyncToolSpecification> mapa = new LinkedHashMap<>();
        // `SyncMcpAnnotationProviders` y no `SyncMcpToolProvider` a secas: el segundo filtra
        // con `method.isAnnotationPresent(McpTool.class)` sobre la clase que le pasen, y sobre
        // un bean de Spring eso puede ser un proxy —donde las anotaciones de los métodos no se
        // heredan— y devuelve CERO herramientas sin decir por qué. Medido: con el proveedor
        // crudo el arranque registraba «Chat con 0 herramientas». Esta variante es la que usa
        // la propia autoconfiguración del servidor MCP, y resuelve el objeto real detrás del
        // proxy antes de mirar las anotaciones.
        for (McpServerFeatures.SyncToolSpecification spec :
                SyncMcpAnnotationProviders.toolSpecifications(List.of(herramientas))) {
            if (PERMITIDAS.contains(spec.tool().name())) {
                mapa.put(spec.tool().name(), spec);
            }
        }
        this.porNombre = Map.copyOf(mapa);
        final Set<String> faltantes = PERMITIDAS.stream()
                .filter(n -> !mapa.containsKey(n))
                .collect(Collectors.toSet());
        if (!faltantes.isEmpty()) {
            // No se falla el arranque: el chat es accesorio y la aplicación tiene que levantar
            // igual. Pero que quede dicho, porque el sintoma sería un modelo que "no sabe".
            logger.warn("El chat esperaba herramientas que el MCP no expone: {}", faltantes);
        }
        logger.info("Chat con {} herramientas: {}", porNombre.size(), porNombre.keySet());
    }

    /** El catálogo en el formato de «tools» que espera la API de OpenAI, que es la que habla pitia. */
    public List<Map<String, Object>> catalogo() {
        return porNombre.values().stream().map(spec -> {
            final McpSchema.Tool tool = spec.tool();
            final Map<String, Object> funcion = new LinkedHashMap<>();
            funcion.put("name", tool.name());
            funcion.put("description", tool.description());
            funcion.put("parameters", tool.inputSchema());
            return Map.<String, Object>of("type", "function", "function", funcion);
        }).toList();
    }

    public boolean existe(String nombre) {
        return porNombre.containsKey(nombre);
    }

    /**
     * Invoca una herramienta y devuelve su resultado como texto para el modelo.
     * <p>
     * El {@code exchange} va en {@code null} y eso está comprobado ejecutándolo, no deducido:
     * ninguna de las herramientas de {@link EkonomiMcpTools} declara un parámetro de ese tipo,
     * que es la única condición bajo la cual el {@code null} se desreferencia. Si alguien
     * agregara una que sí lo declare, esto lanzaría {@code NullPointerException} — y por eso el
     * llamador atrapa {@code Exception} y se lo cuenta al modelo en vez de tumbar la conversación.
     */
    public String invocar(String nombre, Map<String, Object> argumentos) {
        final McpServerFeatures.SyncToolSpecification spec = porNombre.get(nombre);
        if (spec == null) {
            return "{\"error\":\"No existe una herramienta llamada '" + nombre + "'.\"}";
        }
        final McpSchema.CallToolResult resultado = spec.callHandler()
                .apply(null, new McpSchema.CallToolRequest(nombre, argumentos));
        final StringBuilder salida = new StringBuilder();
        for (McpSchema.Content contenido : resultado.content()) {
            if (contenido instanceof McpSchema.TextContent texto) {
                salida.append(texto.text());
            }
        }
        return salida.isEmpty() ? "{}" : salida.toString();
    }
}

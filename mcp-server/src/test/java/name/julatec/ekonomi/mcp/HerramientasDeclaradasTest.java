package name.julatec.ekonomi.mcp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Revisa cómo se le presentan las herramientas al modelo.
 * <p>
 * Un nombre repetido o una descripción vacía no rompen la compilación: rompen la
 * capacidad del cliente de elegir bien, y se notan tarde y en forma de respuesta
 * equivocada. Acá se detectan al construir.
 */
class HerramientasDeclaradasTest {

    private static List<Method> herramientas() {
        final List<Method> metodos = new ArrayList<>();
        for (Method metodo : EkonomiMcpTools.class.getDeclaredMethods()) {
            if (metodo.isAnnotationPresent(McpTool.class)) {
                metodos.add(metodo);
            }
        }
        return metodos;
    }

    @Test
    @DisplayName("están las herramientas que se portaron del servidor en Python, más los reportes")
    void estanTodasLasHerramientas() {
        final Set<String> nombres = new HashSet<>();
        herramientas().forEach(m -> nombres.add(m.getAnnotation(McpTool.class).name()));

        assertTrue(nombres.containsAll(Set.of(
                        "listar_tenants", "estado_ekonomi", "esquema", "buscar_comprobantes",
                        "detalle_comprobante", "resumen_periodo", "clientes_frecuentes", "consulta_sql")),
                "faltan herramientas del servidor en Python: " + nombres);
        assertTrue(nombres.containsAll(Set.of("reporte_compras", "reporte_ventas")),
                "faltan los reportes, que son lo nuevo de esta versión");
    }

    @Test
    @DisplayName("ningún nombre de herramienta se repite")
    void losNombresSonUnicos() {
        final Set<String> vistos = new HashSet<>();
        for (Method metodo : herramientas()) {
            final String nombre = metodo.getAnnotation(McpTool.class).name();
            assertTrue(vistos.add(nombre), "nombre repetido: " + nombre);
        }
    }

    @Test
    @DisplayName("todas se declaran de solo lectura y no destructivas")
    void todasSonDeSoloLectura() {
        for (Method metodo : herramientas()) {
            final McpTool tool = metodo.getAnnotation(McpTool.class);
            assertTrue(tool.annotations().readOnlyHint(),
                    tool.name() + " debería declararse de solo lectura");
            assertFalse(tool.annotations().destructiveHint(),
                    tool.name() + " no debería declararse destructiva");
        }
    }

    @Test
    @DisplayName("cada herramienta y cada parámetro llevan descripción")
    void todoEstaDescrito() {
        for (Method metodo : herramientas()) {
            final McpTool tool = metodo.getAnnotation(McpTool.class);
            assertFalse(tool.description().isBlank(), tool.name() + " no tiene descripción");
            for (Parameter parametro : metodo.getParameters()) {
                final McpToolParam anotacion = parametro.getAnnotation(McpToolParam.class);
                assertNotNull(anotacion,
                        tool.name() + ": el parámetro " + parametro.getName() + " no está anotado");
                assertFalse(anotacion.description().isBlank(),
                        tool.name() + ": el parámetro " + parametro.getName() + " no tiene descripción");
            }
        }
    }

    @Test
    @DisplayName("toda herramienta que toca datos exige el tenant")
    void todasExigenTenant() {
        for (Method metodo : herramientas()) {
            final McpTool tool = metodo.getAnnotation(McpTool.class);
            if ("listar_tenants".equals(tool.name()) || "estado_ekonomi".equals(tool.name())) {
                continue; // son justamente las de orientación previa
            }
            if ("consultar_cabys".equals(tool.name()) || "consultar_actividad".equals(tool.name())) {
                // Ni el catálogo CABYS ni la correspondencia de actividades pertenecen a
                // ninguna contabilidad —los publica Hacienda y son idénticos para todas—, así
                // que no tiene sentido pedirles un tenant. Forzar el parámetro solo para
                // cumplir esta convención mentiría sobre qué datos toca la herramienta.
                continue;
            }
            final Parameter primero = metodo.getParameters()[0];
            assertEquals("tenant", primero.getName(),
                    tool.name() + " debería recibir el tenant como primer parámetro");
            assertTrue(primero.getAnnotation(McpToolParam.class).required(),
                    tool.name() + " no debe permitir omitir el tenant");
        }
    }
}

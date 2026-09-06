package name.julatec.ekonomi.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import name.julatec.ekonomi.actividad.ActividadEconomicaRepository;
import name.julatec.ekonomi.cabys.CabysItemRepository;
import name.julatec.ekonomi.cabys.CabysVersionRepository;
import name.julatec.ekonomi.report.ComprobanteReportService;
import name.julatec.ekonomi.storage.StorageConfig;
import name.julatec.ekonomi.tribunet.storage.FacturaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.server.common.autoconfigure.annotations.McpServerAnnotationScannerAutoConfiguration;
import org.springframework.ai.mcp.server.common.autoconfigure.annotations.McpServerSpecificationFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.ResolvableType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Que las herramientas lleguen de verdad al servidor MCP.
 * <p>
 * Que la clase compile y tenga las anotaciones no prueba nada: quien las
 * convierte en herramientas visibles es el escáner de Spring AI, y si el módulo
 * quedara fuera del component scan —o cambiara el nombre de una anotación en una
 * subida de versión— el WAR seguiría construyéndose y el servidor arrancaría sin
 * una sola herramienta. Esto levanta el escáner real y cuenta lo que publica.
 */
class HerramientasExpuestasTest {

    private static final Set<String> ESPERADAS = Set.of(
            "listar_tenants", "estado_ekonomi", "esquema", "buscar_comprobantes",
            "detalle_comprobante", "resumen_periodo", "clientes_frecuentes", "consulta_sql",
            "reporte_compras", "reporte_ventas", "consultar_cabys", "consultar_actividad");

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    McpServerAnnotationScannerAutoConfiguration.class,
                    McpServerSpecificationFactoryAutoConfiguration.class))
            // `EkonomiMcpTools` inyecta un EntityManager por @PersistenceContext.
            // Acá no se consulta ninguna base —solo se cuentan las herramientas que
            // el escáner publica—, pero la inyección igual tiene que resolver, así
            // que se registra una fábrica de mentira con el nombre de la unidad.
            .withBean(StorageConfig.PERSISTENCE_UNIT, EntityManagerFactory.class, () -> {
                final EntityManagerFactory emf = mock(EntityManagerFactory.class);
                when(emf.createEntityManager()).thenReturn(mock(EntityManager.class));
                return emf;
            })
            .withBean(EkonomiMcpTools.class, () -> new EkonomiMcpTools(
                    mock(AccesoTenant.class),
                    mock(BusquedaComprobantes.class),
                    mock(ResumenComprobantes.class),
                    mock(ComprobanteReportService.class),
                    mock(FacturaRepository.class),
                    mock(CabysItemRepository.class),
                    mock(CabysVersionRepository.class),
                    mock(ActividadEconomicaRepository.class)));

    @SuppressWarnings("unchecked")
    private static List<McpServerFeatures.SyncToolSpecification> especificaciones(
            org.springframework.context.ApplicationContext context) {
        final String[] nombres = context.getBeanNamesForType(ResolvableType.forClassWithGenerics(
                List.class, McpServerFeatures.SyncToolSpecification.class));
        assertTrue(nombres.length > 0, "el escáner no publicó ninguna lista de herramientas");
        return (List<McpServerFeatures.SyncToolSpecification>) context.getBean(nombres[0]);
    }

    @Test
    @DisplayName("el escáner publica exactamente las doce herramientas de Ekonomi")
    void publicaLasOnceHerramientas() {
        contexto.run(context -> {
            assertNull(context.getStartupFailure(), "el contexto no debería fallar");
            final Set<String> publicadas = especificaciones(context).stream()
                    .map(spec -> spec.tool().name())
                    .collect(Collectors.toSet());
            assertEquals(ESPERADAS, publicadas);
        });
    }

    @Test
    @DisplayName("cada herramienta publicada llega con descripción y esquema de entrada")
    void cadaHerramientaLlegaDescrita() {
        contexto.run(context -> {
            for (McpServerFeatures.SyncToolSpecification spec : especificaciones(context)) {
                assertNotNull(spec.tool().description(), spec.tool().name() + " sin descripción");
                assertFalse(spec.tool().description().isBlank(), spec.tool().name() + " sin descripción");
                assertNotNull(spec.tool().inputSchema(), spec.tool().name() + " sin esquema de entrada");
                assertNotNull(spec.callHandler(), spec.tool().name() + " sin implementación");
            }
        });
    }

    @Test
    @DisplayName("se anuncian como de solo lectura, que es lo que el cliente usa para decidir")
    void seAnuncianDeSoloLectura() {
        contexto.run(context -> {
            for (McpServerFeatures.SyncToolSpecification spec : especificaciones(context)) {
                assertNotNull(spec.tool().annotations(), spec.tool().name() + " sin anotaciones");
                assertEquals(Boolean.TRUE, spec.tool().annotations().readOnlyHint(),
                        spec.tool().name() + " debería anunciarse de solo lectura");
            }
        });
    }
}

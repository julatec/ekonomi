package name.julatec.ekonomi.local;

import name.julatec.ekonomi.extract.command.FacturaCompraMapper;
import name.julatec.ekonomi.extract.command.FacturaExportacionMapper;
import name.julatec.ekonomi.extract.command.FacturaMapper;
import name.julatec.ekonomi.extract.command.NotaCreditoMapper;
import name.julatec.ekonomi.extract.command.NotaDebitoMapper;
import name.julatec.ekonomi.tribunet.Documento;
import name.julatec.ekonomi.tribunet.DocumentoAdapterService;
import name.julatec.ekonomi.tribunet.storage.ClaveNota;
import name.julatec.ekonomi.tribunet.storage.FacturaCompraRepository;
import name.julatec.ekonomi.tribunet.storage.FacturaExportacionRepository;
import name.julatec.ekonomi.tribunet.storage.FacturaRepository;
import name.julatec.ekonomi.tribunet.storage.NotaCreditoRepository;
import name.julatec.ekonomi.tribunet.storage.NotaDebitoRepository;
import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * Carga comprobantes de ejemplo en la contabilidad local, para poder abrir el visualizador.
 * <p>
 * La ingesta real vive en {@code tribunet-extractors} y entra por correo: no hay forma de
 * meter un comprobante desde la webapp. Sin datos con XML el detalle responde 409 —la fila
 * existe pero no tiene documento— y no hay nada que ver. Este sembrador usa exactamente los
 * mismos mappers que la ingesta en vez de escribir INSERTs a mano: los nombres de columna los
 * genera Hibernate y adivinarlos es la clase de atajo que después miente.
 * <p>
 * Los XML no se copian al artefacto: el patrón por omisión apunta a los recursos de prueba de
 * {@code tribunet-adapters}, relativo al directorio desde donde corre {@code spring-boot:run}.
 * Así el WAR no lleva facturas de ejemplo ni siquiera desactivadas.
 * <p>
 * Es idempotente por construcción: los mappers hacen upsert sobre la clave, igual que la
 * ingesta cuando el mismo comprobante llega dos veces por correo.
 */
@Component
@Profile("local")
public class SembradorComprobantes implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SembradorComprobantes.class);

    private final DocumentoAdapterService adapterService;
    private final FacturaMapper facturaMapper;
    private final FacturaCompraMapper facturaCompraMapper;
    private final FacturaExportacionMapper facturaExportacionMapper;
    private final NotaCreditoMapper notaCreditoMapper;
    private final NotaDebitoMapper notaDebitoMapper;
    private final FacturaRepository facturaRepository;
    private final FacturaCompraRepository facturaCompraRepository;
    private final FacturaExportacionRepository facturaExportacionRepository;
    private final NotaCreditoRepository notaCreditoRepository;
    private final NotaDebitoRepository notaDebitoRepository;

    @Value("${name.julatec.ekonomi.local.comprobantes:file:../tribunet-adapters/src/test/resources/*.xml}")
    private String patron;

    @Value("${name.julatec.ekonomi.local.tenant:julatec}")
    private String tenant;

    public SembradorComprobantes(
            DocumentoAdapterService adapterService,
            FacturaMapper facturaMapper,
            FacturaCompraMapper facturaCompraMapper,
            FacturaExportacionMapper facturaExportacionMapper,
            NotaCreditoMapper notaCreditoMapper,
            NotaDebitoMapper notaDebitoMapper,
            FacturaRepository facturaRepository,
            FacturaCompraRepository facturaCompraRepository,
            FacturaExportacionRepository facturaExportacionRepository,
            NotaCreditoRepository notaCreditoRepository,
            NotaDebitoRepository notaDebitoRepository) {
        this.adapterService = adapterService;
        this.facturaMapper = facturaMapper;
        this.facturaCompraMapper = facturaCompraMapper;
        this.facturaExportacionMapper = facturaExportacionMapper;
        this.notaCreditoMapper = notaCreditoMapper;
        this.notaDebitoMapper = notaDebitoMapper;
        this.facturaRepository = facturaRepository;
        this.facturaCompraRepository = facturaCompraRepository;
        this.facturaExportacionRepository = facturaExportacionRepository;
        this.notaCreditoRepository = notaCreditoRepository;
        this.notaDebitoRepository = notaDebitoRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        final Resource[] recursos;
        try {
            recursos = resolver.getResources(patron);
        } catch (Exception e) {
            logger.warn("No se pudieron leer los comprobantes de ejemplo de [{}]: {}", patron, e.getMessage());
            return;
        }
        if (recursos.length == 0) {
            logger.info("No hay comprobantes de ejemplo en [{}]; la contabilidad local queda como esté.", patron);
            return;
        }

        // Orden estable para que dos arranques dejen la misma base y los errores se puedan
        // reproducir nombrando el archivo.
        Arrays.sort(recursos, Comparator.comparing(r -> String.valueOf(r.getFilename())));

        try (MultiTenantRepository.Scope ignorado = facturaRepository.openScope(tenant)) {
            int sembrados = 0;
            for (Resource recurso : recursos) {
                if (sembrar(recurso)) {
                    sembrados++;
                }
            }
            logger.info("Comprobantes de ejemplo en [{}]: {} de {} archivos.", tenant, sembrados, recursos.length);
        }
    }

    private boolean sembrar(Resource recurso) {
        final String xml;
        try {
            xml = new String(recurso.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("No se pudo leer [{}]: {}", recurso.getFilename(), e.getMessage());
            return false;
        }

        final Optional<Documento> adaptado = adapterService.adapt(
                xml, e -> logger.warn("No se pudo interpretar [{}]", recurso.getFilename(), e));
        if (adaptado.isEmpty()) {
            return false;
        }

        // El orden importa poco salvo en un punto: `MensajeReceptor` y `MensajeHacienda` no
        // son comprobantes y el adaptador los devuelve igual. Al no coincidir con ninguna
        // rama se descartan solos.
        final Documento documento = adaptado.get();
        return switch (documento) {
            case name.julatec.ekonomi.tribunet.Factura d -> {
                var entidad = facturaMapper.of(facturaRepository.findById(d.getClave()), d);
                entidad.getDocumento().setDocument(xml);
                facturaRepository.save(entidad);
                yield true;
            }
            case name.julatec.ekonomi.tribunet.FacturaCompra d -> {
                var entidad = facturaCompraMapper.of(facturaCompraRepository.findById(d.getClave()), d);
                entidad.getDocumento().setDocument(xml);
                facturaCompraRepository.save(entidad);
                yield true;
            }
            case name.julatec.ekonomi.tribunet.FacturaExportacion d -> {
                var entidad = facturaExportacionMapper.of(facturaExportacionRepository.findById(d.getClave()), d);
                entidad.getDocumento().setDocument(xml);
                facturaExportacionRepository.save(entidad);
                yield true;
            }
            case name.julatec.ekonomi.tribunet.NotaCredito d -> {
                var clave = new ClaveNota().setClave(d.getClave()).setNumeroConsecutivo(d.getNumeroConsecutivo());
                var entidad = notaCreditoMapper.of(notaCreditoRepository.findById(clave), d);
                entidad.getDocumento().setDocument(xml);
                notaCreditoRepository.save(entidad);
                yield true;
            }
            case name.julatec.ekonomi.tribunet.NotaDebito d -> {
                var clave = new ClaveNota().setClave(d.getClave()).setNumeroConsecutivo(d.getNumeroConsecutivo());
                var entidad = notaDebitoMapper.of(notaDebitoRepository.findById(clave), d);
                entidad.getDocumento().setDocument(xml);
                notaDebitoRepository.save(entidad);
                yield true;
            }
            default -> {
                logger.debug("[{}] no es un comprobante ({}); se omite.",
                        recurso.getFilename(), documento.getClass().getSimpleName());
                yield false;
            }
        };
    }
}

package name.julatec.ekonomi.mcp;

import name.julatec.ekonomi.tribunet.DocumentoAdapterService;
import name.julatec.ekonomi.tribunet.storage.ComprobanteSpecs;
import name.julatec.ekonomi.tribunet.storage.Documento;
import name.julatec.ekonomi.tribunet.storage.ElectronicReceipt;
import name.julatec.ekonomi.tribunet.storage.FacturaCompraRepository;
import name.julatec.ekonomi.tribunet.storage.FacturaExportacionRepository;
import name.julatec.ekonomi.tribunet.storage.FacturaRepository;
import name.julatec.ekonomi.tribunet.storage.NotaCreditoRepository;
import name.julatec.ekonomi.tribunet.storage.NotaDebitoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Busca sobre los cinco tipos de comprobante a la vez.
 * <p>
 * No hay superclase JPA común ni tabla común: son cinco entidades en cinco
 * tablas. Así que una búsqueda son cinco consultas y una mezcla, igual que hacía
 * el servidor en Python. Lo que sí es común es la forma —todas embeben
 * {@code Documento}—, y de eso vive {@link ComprobanteSpecs}.
 */
@Service
public class BusquedaComprobantes {

    /** El orden en que se consultan y se reportan. */
    public static final List<String> TIPOS =
            List.of("factura", "factura_compra", "factura_exportacion", "nota_credito", "nota_debito");

    private static final Sort MAS_RECIENTE_PRIMERO =
            Sort.by(Sort.Direction.DESC, "documento.fechaEmision");

    private static final Logger logger = LoggerFactory.getLogger(BusquedaComprobantes.class);

    private final Map<String, JpaSpecificationExecutor<? extends ElectronicReceipt>> repositorios;
    private final DocumentoAdapterService adapterService;

    public BusquedaComprobantes(
            FacturaRepository facturas,
            FacturaCompraRepository facturasCompra,
            FacturaExportacionRepository facturasExportacion,
            NotaCreditoRepository notasCredito,
            NotaDebitoRepository notasDebito,
            DocumentoAdapterService adapterService) {
        final Map<String, JpaSpecificationExecutor<? extends ElectronicReceipt>> mapa = new LinkedHashMap<>();
        mapa.put("factura", facturas);
        mapa.put("factura_compra", facturasCompra);
        mapa.put("factura_exportacion", facturasExportacion);
        mapa.put("nota_credito", notasCredito);
        mapa.put("nota_debito", notasDebito);
        this.repositorios = Map.copyOf(mapa);
        this.adapterService = adapterService;
    }

    public record Resultado(
            int total,
            int devueltos,
            boolean truncado,
            Map<String, Long> porTipo,
            List<ComprobanteResumen> comprobantes) {
    }

    /** Una fila ya resumida, junto con el {@code receipt} del que salió — hace falta su XML si esta fila sobrevive al recorte final. */
    private record Encontrado(ComprobanteResumen resumen, ElectronicReceipt receipt) {
    }

    private <T extends ElectronicReceipt> long contarEn(
            JpaSpecificationExecutor<T> repositorio, FiltroComprobantes filtro) {
        return repositorio.count(filtro.aSpecification());
    }

    private <T extends ElectronicReceipt> List<Encontrado> buscarEn(
            String tipo, JpaSpecificationExecutor<T> repositorio,
            FiltroComprobantes filtro, int limite) {
        final Specification<T> spec = filtro.aSpecification();
        return repositorio.findAll(spec, PageRequest.of(0, limite, MAS_RECIENTE_PRIMERO))
                .getContent()
                .stream()
                .map(receipt -> new Encontrado(ComprobanteResumen.de(tipo, receipt), receipt))
                .toList();
    }

    /**
     * Agrega el desglose de impuesto por tarifa, reparseando el XML del comprobante.
     * <p>
     * Se llama solo sobre la página final —después de mezclar los cinco tipos, ordenar por
     * fecha y recortar al límite pedido—, nunca sobre {@code buscarEn}: ese trae el límite
     * completo de CADA tabla para no perder comprobantes más recientes de otro tipo al
     * mezclar, así que calcular esto antes del recorte reparsearía XML de filas que la
     * paginación final termina descartando.
     */
    private ComprobanteResumen enriquecerConImpuestos(Encontrado encontrado) {
        final String xml = Optional.ofNullable(encontrado.receipt().getDocumento())
                .map(Documento::getDocument)
                .orElse(null);
        if (xml == null || xml.isBlank()) {
            return encontrado.resumen();
        }
        return adapterService.adapt(xml, e -> logger.warn(
                        "No se pudo reparsear el XML de {} para el desglose de impuesto",
                        encontrado.resumen().clave(), e))
                .map(documento -> encontrado.resumen().conImpuestos(ComprobanteResumen.tasasDe(documento)))
                .orElse(encontrado.resumen());
    }

    private <T extends ElectronicReceipt> Optional<T> unoPorClave(
            JpaSpecificationExecutor<T> repositorio, String clave) {
        return repositorio.findAll(ComprobanteSpecs.<T>clave(clave), PageRequest.of(0, 1))
                .getContent().stream().findFirst();
    }

    public Set<String> tiposConocidos() {
        return repositorios.keySet();
    }

    /**
     * Conteo de los <b>cinco</b> tipos para el mismo filtro, sin traer filas.
     * <p>
     * {@link #buscar} solo cuenta los tipos consultados, que es lo correcto para una
     * herramienta. Para una UI con filtros por tipo no alcanza: al dejar solo "factura"
     * marcado, los otros cuatro se quedarían sin número y no habría con qué volver a
     * encenderlos. Son cinco {@code count()}, sin materializar entidades.
     */
    public Map<String, Long> conteosPorTipo(FiltroComprobantes filtro) {
        final Map<String, Long> porTipo = new LinkedHashMap<>();
        for (String tipo : TIPOS) {
            porTipo.put(tipo, contarEn(repositorios.get(tipo), filtro));
        }
        return porTipo;
    }

    /**
     * @param tipos si viene vacío, se consultan los cinco
     * @param limite tope de filas devueltas en total, ya mezcladas
     */
    public Resultado buscar(FiltroComprobantes filtro, Set<String> tipos, int limite) {
        final List<String> aConsultar = TIPOS.stream()
                .filter(tipo -> tipos == null || tipos.isEmpty() || tipos.contains(tipo))
                .toList();

        final Map<String, Long> porTipo = new LinkedHashMap<>();
        final List<Encontrado> encontrados = new ArrayList<>();
        long total = 0;
        for (String tipo : aConsultar) {
            final JpaSpecificationExecutor<? extends ElectronicReceipt> repositorio = repositorios.get(tipo);
            final long cuantos = contarEn(repositorio, filtro);
            porTipo.put(tipo, cuantos);
            total += cuantos;
            if (cuantos > 0) {
                // Se pide el tope completo a cada tabla porque el orden final es
                // por fecha entre todas: quedarse con menos de una podría dejar
                // fuera comprobantes más recientes que los de otra.
                encontrados.addAll(buscarEn(tipo, repositorio, filtro, limite));
            }
        }

        // Por el INSTANTE real (fecha Y hora), no por ComprobanteResumen.fechaEmision(): esa es
        // una cadena "aaaa-mm-dd" pensada para mostrarse en la UI, sin la hora. Ordenando por
        // esa cadena, dos comprobantes del mismo día quedaban desempatados por clave -orden
        // alfabético de un identificador, no de cuándo se emitió cada uno-, así que un
        // comprobante de las 8 a.m. podía aparecer después de otro de las 5 p.m. del mismo día.
        encontrados.sort(Comparator
                .comparing((Encontrado e) -> Optional.ofNullable(e.receipt().getDocumento())
                                .map(Documento::getFechaEmision)
                                .orElse(null),
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(e -> e.resumen().clave(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        final List<Encontrado> recortado = encontrados.size() > limite
                ? List.copyOf(encontrados.subList(0, limite))
                : List.copyOf(encontrados);

        final List<ComprobanteResumen> pagina = recortado.stream()
                .map(this::enriquecerConImpuestos)
                .toList();

        return new Resultado((int) total, pagina.size(), total > pagina.size(), porTipo, pagina);
    }

    /** El comprobante con esa clave exacta, buscando en los cinco tipos. */
    public Optional<Map.Entry<String, ElectronicReceipt>> porClave(String clave) {
        for (String tipo : TIPOS) {
            final Optional<? extends ElectronicReceipt> encontrado =
                    unoPorClave(repositorios.get(tipo), clave);
            if (encontrado.isPresent()) {
                return Optional.of(Map.entry(tipo, encontrado.get()));
            }
        }
        return Optional.empty();
    }
}

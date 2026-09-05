package name.julatec.ekonomi.mcp;

import name.julatec.ekonomi.tribunet.storage.ComprobanteSpecs;
import name.julatec.ekonomi.tribunet.storage.ElectronicReceipt;
import name.julatec.ekonomi.tribunet.storage.FacturaCompraRepository;
import name.julatec.ekonomi.tribunet.storage.FacturaExportacionRepository;
import name.julatec.ekonomi.tribunet.storage.FacturaRepository;
import name.julatec.ekonomi.tribunet.storage.NotaCreditoRepository;
import name.julatec.ekonomi.tribunet.storage.NotaDebitoRepository;
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

    private final Map<String, JpaSpecificationExecutor<? extends ElectronicReceipt>> repositorios;

    public BusquedaComprobantes(
            FacturaRepository facturas,
            FacturaCompraRepository facturasCompra,
            FacturaExportacionRepository facturasExportacion,
            NotaCreditoRepository notasCredito,
            NotaDebitoRepository notasDebito) {
        final Map<String, JpaSpecificationExecutor<? extends ElectronicReceipt>> mapa = new LinkedHashMap<>();
        mapa.put("factura", facturas);
        mapa.put("factura_compra", facturasCompra);
        mapa.put("factura_exportacion", facturasExportacion);
        mapa.put("nota_credito", notasCredito);
        mapa.put("nota_debito", notasDebito);
        this.repositorios = Map.copyOf(mapa);
    }

    public record Resultado(
            int total,
            int devueltos,
            boolean truncado,
            Map<String, Long> porTipo,
            List<ComprobanteResumen> comprobantes) {
    }

    private <T extends ElectronicReceipt> long contarEn(
            JpaSpecificationExecutor<T> repositorio, FiltroComprobantes filtro) {
        return repositorio.count(filtro.aSpecification());
    }

    private <T extends ElectronicReceipt> List<ComprobanteResumen> buscarEn(
            String tipo, JpaSpecificationExecutor<T> repositorio,
            FiltroComprobantes filtro, int limite) {
        final Specification<T> spec = filtro.aSpecification();
        return repositorio.findAll(spec, PageRequest.of(0, limite, MAS_RECIENTE_PRIMERO))
                .getContent()
                .stream()
                .map(receipt -> ComprobanteResumen.de(tipo, receipt))
                .toList();
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
     * @param tipos si viene vacío, se consultan los cinco
     * @param limite tope de filas devueltas en total, ya mezcladas
     */
    public Resultado buscar(FiltroComprobantes filtro, Set<String> tipos, int limite) {
        final List<String> aConsultar = TIPOS.stream()
                .filter(tipo -> tipos == null || tipos.isEmpty() || tipos.contains(tipo))
                .toList();

        final Map<String, Long> porTipo = new LinkedHashMap<>();
        final List<ComprobanteResumen> encontrados = new ArrayList<>();
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

        encontrados.sort(Comparator
                .comparing(ComprobanteResumen::fechaEmision,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ComprobanteResumen::clave,
                        Comparator.nullsLast(Comparator.naturalOrder())));

        final List<ComprobanteResumen> pagina = encontrados.size() > limite
                ? List.copyOf(encontrados.subList(0, limite))
                : List.copyOf(encontrados);

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

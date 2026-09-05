package name.julatec.ekonomi.report;

import name.julatec.ekonomi.accounting.Voucher;
import name.julatec.ekonomi.tribunet.Documento;
import name.julatec.ekonomi.tribunet.DocumentoAdapterService;
import name.julatec.ekonomi.tribunet.InformacionReferencia;
import name.julatec.ekonomi.tribunet.storage.ElectronicReceipt;
import name.julatec.ekonomi.tribunet.storage.FacturaCompraRepository;
import name.julatec.ekonomi.tribunet.storage.FacturaRepository;
import name.julatec.ekonomi.tribunet.storage.NotaCreditoRepository;
import name.julatec.ekonomi.tribunet.storage.NotaDebitoRepository;
import name.julatec.util.algebraic.Interval;
import org.apache.commons.lang3.builder.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Arma los reportes auxiliares de compras y ventas.
 * <p>
 * Esta lógica vivía en {@code webapp.report.ReportService}, atada a un
 * {@code Workspace} —un objeto de sesión web— del que en realidad solo usaba el
 * rango de fechas. Bajarla a {@code storage} y recibir el {@link Interval}
 * directamente la deja disponible para cualquier consumidor: el controlador
 * HTTP de siempre y, ahora, las herramientas MCP. El {@code ReportService} del
 * webapp quedó como una fachada que traduce {@code Workspace} a rango.
 */
@Service
public class ComprobanteReportService {

    private static final Logger logger = LoggerFactory.getLogger(ComprobanteReportService.class);

    private final FacturaRepository facturaRepository;
    private final FacturaCompraRepository facturaCompraRepository;
    private final NotaCreditoRepository notaCreditoRepository;
    private final NotaDebitoRepository notaDebitoRepository;
    private final DocumentoAdapterService documentoAdapterService;

    public ComprobanteReportService(
            FacturaRepository facturaRepository,
            FacturaCompraRepository facturaCompraRepository,
            NotaCreditoRepository notaCreditoRepository,
            NotaDebitoRepository notaDebitoRepository,
            DocumentoAdapterService documentoAdapterService) {
        this.facturaRepository = facturaRepository;
        this.facturaCompraRepository = facturaCompraRepository;
        this.notaCreditoRepository = notaCreditoRepository;
        this.notaDebitoRepository = notaDebitoRepository;
        this.documentoAdapterService = documentoAdapterService;
    }

    public SortedSet<Voucher> purchases(String numero, Interval<Date> range) {
        return new PaperVoucherSetBuilder(numero, range.lower, range.upper, isPurchase(numero))
                .add(facturaRepository::searchByRepecetor)
                .add(notaCreditoRepository::search)
                .add(notaDebitoRepository::search)
                .add(facturaCompraRepository::searchByRepecetor)
                .build();
    }

    public SortedSet<Voucher> sales(String numero, Interval<Date> range) {
        return new PaperVoucherSetBuilder(numero, range.lower, range.upper, isSale(numero))
                .add(facturaRepository::searchByEmisor)
                .add(notaCreditoRepository::search)
                .add(notaDebitoRepository::search)
                .add(facturaCompraRepository::searchByEmisor)
                .build();
    }

    private Predicate<InformacionReferencia> isPurchase(String numero) {
        return informacionReferencia -> {
            final String clave = informacionReferencia.getNumero();
            for (String emisor = facturaRepository.getReceptorByClave(clave); emisor != null; ) {
                return emisor.equals(numero);
            }
            for (String emisor = facturaCompraRepository.getReceptorByClave(clave); emisor != null; ) {
                return emisor.equals(numero);
            }
            return true;
        };
    }

    private Predicate<InformacionReferencia> isSale(String numero) {
        return informacionReferencia -> {
            final String clave = informacionReferencia.getNumero();
            for (String emisor = facturaRepository.getEmisorByClave(clave); emisor != null; ) {
                return emisor.equals(numero);
            }
            for (String emisor = facturaCompraRepository.getEmisorByClave(clave); emisor != null; ) {
                return emisor.equals(numero);
            }
            return false;
        };
    }

    private class PaperVoucherSetBuilder implements Builder<SortedSet<Voucher>> {

        private final String id;
        private final Date lower;
        private final Date upper;
        private final Predicate<InformacionReferencia> notaFilter;
        private final SortedSet<Voucher> result = new TreeSet<>();

        private PaperVoucherSetBuilder(String id, Date lower, Date upper,
                                       Predicate<InformacionReferencia> notaFilter) {
            this.id = id;
            this.lower = lower;
            this.upper = upper;
            this.notaFilter = notaFilter;
        }

        private Documento adapt(ElectronicReceipt electronicReceipt) {
            logger.info("Adapting {}", electronicReceipt.getClave());
            return documentoAdapterService.adapt(
                    electronicReceipt.getDocumento().getDocument(),
                    e -> logger.error("Unable to adapt {}.", electronicReceipt.getClave(), e)
            ).orElseThrow(() -> new NullPointerException("Unable to adapt document."));
        }

        private boolean filterNotas(Documento documento) {
            if (documento instanceof name.julatec.ekonomi.tribunet.NotaCredito ||
                    documento instanceof name.julatec.ekonomi.tribunet.NotaDebito) {
                return documento.getInformacionReferencia()
                        .map(notaFilter::test)
                        .max(Boolean::compareTo)
                        .orElse(true);
            }
            return true;
        }

        private <T extends ElectronicReceipt> PaperVoucherSetBuilder add(ElectronicReceiptProvider<T> provider) {
            provider.search(id, lower, upper)
                    .stream()
                    .map(this::adapt)
                    .filter(this::filterNotas)
                    .map(Voucher::of)
                    .forEach(result::add);
            return this;
        }

        @Override
        public SortedSet<Voucher> build() {
            return result;
        }
    }

    @FunctionalInterface
    private interface ElectronicReceiptProvider<T extends ElectronicReceipt> {
        List<T> search(String id, Date lower, Date upper);
    }
}

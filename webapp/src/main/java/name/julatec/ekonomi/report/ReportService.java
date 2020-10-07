package name.julatec.ekonomi.report;

import name.julatec.ekonomi.accounting.BankOperationRepository;
import name.julatec.ekonomi.accounting.BankTransactionRepository;
import name.julatec.ekonomi.accounting.Voucher;
import name.julatec.ekonomi.accounting.VoucherRepository;
import name.julatec.ekonomi.report.bank.BankOperation;
import name.julatec.ekonomi.report.bank.BankTransaction;
import name.julatec.ekonomi.security.ImportBankOperation;
import name.julatec.ekonomi.security.ImportBankTransaction;
import name.julatec.ekonomi.security.ImportManualTransaction;
import name.julatec.ekonomi.session.Workspace;
import name.julatec.ekonomi.tribunet.Documento;
import name.julatec.ekonomi.tribunet.DocumentoAdapterService;
import name.julatec.ekonomi.tribunet.InformacionReferencia;
import name.julatec.ekonomi.tribunet.storage.*;
import name.julatec.util.algebraic.Interval;
import org.apache.commons.lang3.builder.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    ApplicationContext applicationContext;

    FacturaRepository facturaRepository;

    FacturaCompraRepository facturaCompraRepository;

    NotaCreditoRepository notaCreditoRepository;

    NotaDebitoRepository notaDebitoRepository;

    DocumentoAdapterService documentoAdapterService;

    BankTransactionRepository bankTransactionRepository;

    VoucherRepository voucherRepository;

    BankOperationRepository bankOperationRepository;

//    Stream<Record> getRecordStream(String numero, Workspace workspace) {
//        final Interval<Date> range = workspace.getDateInterval();
//        final Stream<Record> electronicReceipts = Stream.of(
//                facturaRepository.searchByRepecetor(numero, range.lower, range.upper).stream().map(Record::new),
//                notaCreditoRepository.searchByRepecetor(numero, range.lower, range.upper).stream().map(Record::new),
//                notaDebitoRepository.searchByRepecetor(numero, range.lower, range.upper).stream().map(Record::new))
//                .flatMap(Stream::parallel)
//                .sorted();
//        final Stream<name.julatec.ekonomi.accounting.BankTransaction> bankReceipts = bankTransactionRepository.search(range);
//        final Stream<PaperVoucher> paperTransactions = transactionRepository
//                .searchByRepecetor(numero, range.lower, range.upper)
//                .stream()
//                .flatMap(transaction -> {
//                    try {
//                        return Stream.of(transaction.setManualTransaction(Optional.of(Clave.of(transaction.getClave()).toUuid())));
//                    } catch (Exception e) {
//                        logger.error("Unable to assign clave to bank transaction", e);
//                        return Stream.empty();
//                    }
//                });
//        final Stream<Record> recordWithBankStream = Record.mergeWithBank(electronicReceipts, bankReceipts);
//        return Record.mergeWithPaper(recordWithBankStream, paperTransactions);
//    }
//
//    public SortedSet<RecordSummary> transactions(String numero, Workspace workspace) {
//        final Stream<Record> recordWithBankAndPaperStream = getRecordStream(numero, workspace);
//        return recordWithBankAndPaperStream
//                .map(RecordSummary::new)
//                .collect(Collectors.toCollection(TreeSet::new));
//    }

    public SortedSet<Voucher> purchases(String numero, Workspace workspace) {
        Interval<Date> range = workspace.getDateInterval();
        return new PaperVoucherSetBuilder(numero, range.lower, range.upper, isPurchase(numero))
                .add(facturaRepository::searchByRepecetor)
                .add(notaCreditoRepository::search)
                .add(notaDebitoRepository::search)
                .add(facturaCompraRepository::searchByRepecetor)
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

    public SortedSet<Voucher> sales(String numero, Workspace workspace) {
        Interval<Date> range = workspace.getDateInterval();
        return new PaperVoucherSetBuilder(numero, range.lower, range.upper, isSale(numero))
                .add(facturaRepository::searchByEmisor)
                .add(notaCreditoRepository::search)
                .add(notaDebitoRepository::search)
                .add(facturaCompraRepository::searchByEmisor)
                .build();

    }

    public <T extends BankTransaction<T>> long upload(Workspace workspace, ImportBankTransaction account, Stream<T> stream) {
        try {
            return stream
                    .map(name.julatec.ekonomi.accounting.BankTransaction::of)
                    .map(bankTransactionRepository::saveAndFlush)
                    .map(transaction -> {
                        logger.info("Imported {}", transaction);
                        return transaction;
                    })
                    .count();
        } catch (Exception e) {
            final String message = workspace.getLocalizedMessage(Message.UnableToUploadReport, e.getMessage());
            logger.error(message, e);
            throw new IllegalArgumentException(message, e);
        }
    }

    public <T extends BankOperation<T>> long upload(Workspace workspace, ImportBankOperation account, Stream<T> stream) {
        try {
            return stream
                    .map(account::of)
                    .map(bankOperationRepository::saveAndFlush)
                    .map(transaction -> {
                        logger.info("Imported {}", transaction);
                        return transaction;
                    })
                    .count();
        } catch (Exception e) {
            final String message = workspace.getLocalizedMessage(Message.UnableToUploadReport, e.getMessage());
            logger.error(message, e);
            throw new IllegalArgumentException(message, e);
        }
    }

    public long upload(Workspace workspace, ImportManualTransaction account, Stream<Voucher> stream) {
        try {
            return stream
                    .filter(transaction -> transaction.getClave() != null)
                    .map(voucherRepository::saveAndFlush)
                    .map(transaction -> {
                        logger.info("Imported {}", transaction);
                        return transaction;
                    })
                    .count();
        } catch (Exception e) {
            final String message = workspace.getLocalizedMessage(Message.UnableToUploadReport, e.getMessage());
            logger.error(message, e);
            throw new IllegalArgumentException(message, e);
        }
    }

    @Autowired
    ReportService setFacturaRepository(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
        return this;
    }

    @Autowired
    ReportService setFacturaCompraRepository(FacturaCompraRepository facturaCompraRepository) {
        this.facturaCompraRepository = facturaCompraRepository;
        return this;
    }

    @Autowired
    ReportService setNotaCreditoRepository(NotaCreditoRepository notaCreditoRepository) {
        this.notaCreditoRepository = notaCreditoRepository;
        return this;
    }

    @Autowired
    ReportService setNotaDebitoRepository(NotaDebitoRepository notaDebitoRepository) {
        this.notaDebitoRepository = notaDebitoRepository;
        return this;
    }


    @Autowired
    ReportService setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }

    @Autowired
    ReportService setDocumentoAdapterService(DocumentoAdapterService documentoAdapterService) {
        this.documentoAdapterService = documentoAdapterService;
        return this;
    }

    @Autowired
    ReportService setBankTransactionRepository(BankTransactionRepository bankTransactionRepository) {
        this.bankTransactionRepository = bankTransactionRepository;
        return this;
    }

    @Autowired
    ReportService setTransactionRepository(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
        return this;
    }

    @Autowired
    ReportService setBankOperationRepository(BankOperationRepository bankOperationRepository) {
        this.bankOperationRepository = bankOperationRepository;
        return this;
    }

    enum Message {
        UnableToUploadReport
    }

    private class PaperVoucherSetBuilder implements Builder<SortedSet<Voucher>> {

        private final String id;
        private final Date lower;
        private final Date upper;
        private final Predicate<InformacionReferencia> notaFilter;
        private final SortedSet<Voucher> result = new TreeSet<>();

        private Documento adapt(ElectronicReceipt electronicReceipt) {
            logger.info("Adapting {}", electronicReceipt.getClave());
            return documentoAdapterService.adapt(
                    electronicReceipt.getDocumento().getDocument(),
                    e -> logger.error("Unable to adapt {}.", electronicReceipt.getClave(), e)
            ).orElseThrow(() -> new NullPointerException("Unable to adapt document."));
        }

        private PaperVoucherSetBuilder(String id, Date lower, Date upper, Predicate<InformacionReferencia> notaFilter) {
            this.id = id;
            this.lower = lower;
            this.upper = upper;
            this.notaFilter = notaFilter;
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

        public <T extends ElectronicReceipt> PaperVoucherSetBuilder add(ElectronicReceiptProvider<T> provider) {
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

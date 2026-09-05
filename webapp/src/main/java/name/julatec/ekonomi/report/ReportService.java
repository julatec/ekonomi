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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.SortedSet;
import java.util.stream.Stream;

/**
 * Fachada web sobre {@link ComprobanteReportService}.
 * <p>
 * El armado de los reportes bajó a {@code storage} para que también lo puedan
 * usar las herramientas MCP, que no tienen sesión web. Lo que queda acá es lo
 * que sí es del webapp: traducir el {@link Workspace} de la sesión a un rango
 * de fechas, y las importaciones de estados de cuenta.
 */
@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    ComprobanteReportService comprobantes;

    BankTransactionRepository bankTransactionRepository;

    VoucherRepository voucherRepository;

    BankOperationRepository bankOperationRepository;

    public SortedSet<Voucher> purchases(String numero, Workspace workspace) {
        return comprobantes.purchases(numero, workspace.getDateInterval());
    }

    public SortedSet<Voucher> sales(String numero, Workspace workspace) {
        return comprobantes.sales(numero, workspace.getDateInterval());
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
    ReportService setComprobantes(ComprobanteReportService comprobantes) {
        this.comprobantes = comprobantes;
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
}

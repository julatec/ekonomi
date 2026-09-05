package name.julatec.ekonomi.report;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import name.julatec.ekonomi.accounting.Voucher;
import name.julatec.ekonomi.report.bank.BankOperation;
import name.julatec.ekonomi.report.bank.BankTransaction;
import name.julatec.ekonomi.security.ImportBankOperation;
import name.julatec.ekonomi.security.ImportBankTransaction;
import name.julatec.ekonomi.security.ImportManualTransaction;
import name.julatec.ekonomi.security.ImportTransaction;
import name.julatec.ekonomi.session.Workspace;
import name.julatec.ekonomi.session.WorkspaceService;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import static name.julatec.ekonomi.report.ReportController.Message.*;
import static name.julatec.ekonomi.tribunet.UuidFormatter.uuidFromString;
import static name.julatec.ekonomi.tribunet.UuidFormatter.uuidToString;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@SuppressWarnings("DuplicatedCode")
public class ReportController {

    public static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String PROLOG = "application/prolog";
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    @Autowired
    ReportService service;
    @Autowired
    WorkspaceService workspaceService;

    public static String getUploadTransactionReportEndpoint(ImportTransaction importBankAccount) {
        return String.format("./upload/%s", uuidToString(importBankAccount.getAccount()));
    }

    @GetMapping(value = "/report/purchases", produces = XLSX)
    public void generatePurchasesReport2(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "id") String id) {
        final Workspace workspace = workspaceService.getWorkspace(authentication, request);
        final String reportName = workspace.getLocalizedMessage(PurchasesReportName,
                id, workspace.getDateInterval().lower, workspace.getDateInterval().upper);
        try {
            final Iterable<Voucher> transactionList = service.purchases(id, workspace);
            generateTransactionReport(response, reportName, transactionList);
        } catch (Exception ex) {
            final String message = workspace.getLocalizedMessage(ReportNotGenerated, ex.getMessage());
            logger.error(message, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, message, ex);
        }
    }

    @GetMapping(value = "/report/sales", produces = XLSX)
    public void generateSalesReport2(
            HttpServletRequest request,
            Authentication authentication,
            HttpServletResponse response,
            @RequestParam(value = "id") String id) {
        final Workspace workspace = workspaceService.getWorkspace(authentication, request);
        final String reportName = workspace.getLocalizedMessage(SalesReportName,
                id, workspace.getDateInterval().lower, workspace.getDateInterval().upper).replace('/', '-');
        try {
            final Iterable<Voucher> transactionList = service.sales(id, workspace);
            generateTransactionReport(response, reportName, transactionList);
        } catch (Exception ex) {
            final String message = workspace.getLocalizedMessage(ReportNotGenerated, ex.getMessage());
            logger.error(message, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, message, ex);
        }
    }

    private void generateTransactionReport(HttpServletResponse response, String reportName, Iterable<Voucher> transactionList)
            throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        final Workbook workbook = Voucher.toWorkbook(transactionList);
        reportName = reportName.replace(',', '-');
        response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", reportName));
        workbook.write(response.getOutputStream());
    }

    /**
     * Importa un estado de cuenta a la cuenta indicada.
     * <p>
     * La cuenta trae su propia unidad de persistencia, que no tiene por qué coincidir con el
     * tenant activo de la cookie; el cambio de ámbito lo hace {@link ReportService}, que es
     * quien tiene los repositorios.
     */
    @PostMapping(value = "/upload/{account}")
    public ResponseEntity<Map<String, Object>> upload(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable(value = "account") String account,
            @RequestParam("file") MultipartFile file) {
        final Workspace workspace = workspaceService.getWorkspace(authentication, request);
        final ImportTransaction<?> importAccount = workspace.getImportBankAccount(uuidFromString(account));
        if (importAccount == null) {
            final String message = workspace.getLocalizedMessage(AccountNotConfigured, account);
            logger.warn(message);
            throw new ResponseStatusException(NOT_FOUND, message);
        }
        try (InputStream input = file.getInputStream()) {
            final long imported = switch (importAccount) {
                case ImportBankTransaction bank -> importTransactions(workspace, bank, input, account);
                case ImportBankOperation operation -> importOperations(workspace, operation, input);
                case ImportManualTransaction manual -> service.upload(workspace, manual,
                        Voucher.fromCsv(input, Optional.of(manual.getDefaultCurrency())));
                default -> throw new IllegalStateException(
                        "Tipo de cuenta de importación no soportado: " + importAccount.getClass().getName());
            };
            return ResponseEntity.ok(Map.of(
                    "imported", imported,
                    "message", workspace.getLocalizedMessage(ImportedRecords, account, imported)));
        } catch (Exception ex) {
            final String message = workspace.getLocalizedMessage(RecordNotRead, account, ex.getMessage());
            logger.error(message, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, message, ex);
        }
    }

    /**
     * El parámetro de tipo se liga una sola vez acá: {@code getClazz()} declara su propia
     * variable de tipo, y en una sola expresión javac no puede unirla con la de
     * {@code stream(...)}.
     */
    private <T extends BankTransaction<T>> long importTransactions(
            Workspace workspace, ImportBankTransaction account, InputStream input, String cuenta)
            throws ClassNotFoundException {
        final Class<T> clazz = account.getClazz();
        return service.upload(workspace, account,
                BankTransaction.stream(clazz, input, cuenta, Optional.of(account.getDefaultCurrency())));
    }

    private <T extends BankOperation<T>> long importOperations(
            Workspace workspace, ImportBankOperation account, InputStream input)
            throws ClassNotFoundException {
        final Class<T> clazz = account.getClazz();
        return service.upload(workspace, account,
                BankOperation.stream(clazz, input, Optional.of(account.getDefaultCurrency())));
    }

    enum Message {
        SalesReportName,
        PurchasesReportName,
        ReportNotGenerated,
        UnsupportedReport,
        AccountNotConfigured,
        ImportedRecords,
        RecordNotRead
    }


}

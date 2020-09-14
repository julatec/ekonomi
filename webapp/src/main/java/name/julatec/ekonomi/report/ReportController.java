package name.julatec.ekonomi.report;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import name.julatec.ekonomi.accounting.PaperVoucher;
import name.julatec.ekonomi.accounting.Record;
import name.julatec.ekonomi.security.ImportTransaction;
import name.julatec.ekonomi.session.Workspace;
import name.julatec.ekonomi.session.WorkspaceService;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Stream;
import static name.julatec.ekonomi.report.ReportController.Message.*;
import static name.julatec.ekonomi.tribunet.UuidFormatter.uuidToString;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

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
            final Iterable<PaperVoucher> transactionList = service.purchases(id, workspace);
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
            final Iterable<PaperVoucher> transactionList = service.sales(id, workspace);
            generateTransactionReport(response, reportName, transactionList);
        } catch (Exception ex) {
            final String message = workspace.getLocalizedMessage(ReportNotGenerated, ex.getMessage());
            logger.error(message, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, message, ex);
        }
    }

    @GetMapping(value = "/report/transactions", produces = XLSX)
    public void generateTransactionsReport2(
            HttpServletRequest request,
            Authentication authentication,
            HttpServletResponse response,
            @RequestParam(value = "id") String id) {
        final Workspace workspace = workspaceService.getWorkspace(authentication, request);
        final String reportName = workspace.getLocalizedMessage(TransactionsReportName,
                id, workspace.getDateInterval().lower, workspace.getDateInterval().upper) + ".pl";
        try {
            final Stream<Record> transactionList = Stream.empty();
                    //service.getRecordStream(id, workspace);
            generatePrologReport(response, reportName, transactionList);
        } catch (Exception ex) {
            final String message = workspace.getLocalizedMessage(ReportNotGenerated, ex.getMessage());
            logger.error(message, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, message, ex);
        }
    }

    private void generatePrologReport(HttpServletResponse response, String reportName, Stream<Record> records) throws IOException {
//        response.addHeader("Content-Disposition", String.format("attachment; filename=%s", reportName));
//        try (final Printer printer = new Printer(response.getWriter(), "transactions")) {
//            records.forEach(printer::print);
//        }
    }

    private void generateTransactionReport(HttpServletResponse response, String reportName, Iterable<PaperVoucher> transactionList)
            throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        final Workbook workbook = PaperVoucher.toWorkbook(transactionList);
        response.addHeader("Content-Disposition", String.format("attachment; filename=%s", reportName));
        workbook.write(response.getOutputStream());
    }

    private void generateRecordReport(HttpServletResponse response, String reportName, Iterable<RecordSummary> transactionList)
            throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        final Workbook workbook = RecordSummary.toWorkbook(transactionList);
        response.addHeader("Content-Disposition", String.format("attachment; filename=%s", reportName));
        workbook.write(response.getOutputStream());
    }

    @PostMapping(value = "/upload/{account}")
    public String upload(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable(value = "account") String account,
            @RequestParam("file") MultipartFile file) throws IOException, ClassNotFoundException {
        return "faild";
    }

    enum Message {
        SalesReportName,
        PurchasesReportName,
        TransactionsReportName,
        ReportNotGenerated,
        UnsupportedReport,
        AccountNotConfigured,
        ImportedRecords,
        RecordNotRead
    }


}

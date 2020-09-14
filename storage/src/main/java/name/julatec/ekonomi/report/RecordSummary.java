package name.julatec.ekonomi.report;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import name.julatec.ekonomi.report.csv.CsvBindByNameOrder;
import name.julatec.ekonomi.report.csv.CsvCurrencyConverter;
import name.julatec.ekonomi.report.csv.MappingStrategy;
import name.julatec.ekonomi.accounting.Record;
import name.julatec.ekonomi.gnucash.Account;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.poi.ss.usermodel.Workbook;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

@CsvBindByNameOrder({
        "Fecha",
        "B",
        "E",
        "P",
        "Source",
        "Target",
        "Moneda",
        "Total Comprobante",
        "Emisor",
        "Nombre Emisor",
        "Receptor",
        "Nombre Receptor",
        "Total Exento",
        "Total Gravado",
        "Total Impuesto",
        "Total Impuesto Devuelto",
        "Clave",
        "Consecutivo",
})
public class RecordSummary implements Comparable<RecordSummary> {

    public static final MappingStrategy MAPPING_STRATEGY = new MappingStrategy<>(RecordSummary.class);

    @CsvBindByName(column = "Clave")
    private final String clave;

    @CsvBindByName(column = "Consecutivo")
    private final String consecutivo;

    @CsvBindByName(column = "Fecha")
    @CsvDate("yyyy/MM/dd")
    private final Date fecha;

    @CsvBindByName(column = "Emisor")
    private final String emisorNumero;

    @CsvBindByName(column = "Receptor")
    private final String receptorNumero;

    @CsvBindByName(column = "Nombre Emisor")
    private final String emisorNombre;

    @CsvBindByName(column = "Nombre Receptor")
    private final String receptorNombre;

    @CsvBindByName(column = "Total Gravado", locale = "es_CR")
    private final BigDecimal totalGravado;

    @CsvBindByName(column = "Total Exento", locale = "es_CR")
    private final BigDecimal totalExento;

    @CsvBindByName(column = "Total Impuesto", locale = "es_CR")
    private final BigDecimal totalImpuesto;

    @CsvBindByName(column = "Total Impuesto Devuelto", locale = "es_CR")
    private final BigDecimal totalImpuestoDevuelto;

    @CsvBindByName(column = "Total Comprobante", locale = "es_CR")
    private final BigDecimal totalComprobante;

    @CsvBindByName(column = "B")
    private final Boolean hasBank;

    @CsvBindByName(column = "E")
    private final Boolean hasElectronic;

    @CsvBindByName(column = "P")
    private final Boolean hasPaper;

    @CsvBindByName(column = "Source")
    private final String sourceAccount;

    @CsvBindByName(column = "Target")
    private final String targetAccount;

    @CsvCustomBindByName(column = "Moneda", converter = CsvCurrencyConverter.class)
    private final Currency currency;

    public RecordSummary(Record record) {
        this.clave = record.getElectronicReceipt().isPresent() ? record.getElectronicReceipt().get().getRecordKey() :
                record.getPaperReceipt().isPresent() ? record.getPaperReceipt().get().getClave() :
                        record.getBankReceipt().get().getId().toString();
        this.consecutivo = record.getElectronicReceipt().isPresent() ? record.getElectronicReceipt().get().getRecordSequential() :
                record.getPaperReceipt().isPresent() ? record.getPaperReceipt().get().getConsecutivo() :
                        record.getBankReceipt().get().getDocumentNumber();
        this.fecha = record.getKey().date.get();
        this.currency = record.getKey().currency.get();
        this.totalComprobante = record.getKey().total.get();
        this.emisorNumero = record.getEmitterId().orElse(null);
        this.emisorNombre = record.getEmitterName().orElse(null);
        this.receptorNumero = record.getReceptorId().orElse(null);
        this.receptorNombre = record.getReceptorName().orElse(null);
        this.hasBank = record.getBankReceipt().isPresent();
        this.hasElectronic = record.getElectronicReceipt().isPresent();
        this.hasPaper = record.getPaperReceipt().isPresent();
        this.sourceAccount = record.getSourceAccount().map(Account::getDescription).orElse(null);
        this.targetAccount = record.getTargetAccount().map(Account::getDescription).orElse(null);
        this.totalGravado = null;
        this.totalExento = null;
        this.totalImpuesto = null;
        this.totalImpuestoDevuelto = null;
    }

    public static Workbook toWorkbook(Iterable<RecordSummary> transactionList) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        return MAPPING_STRATEGY.toWorkbook(transactionList);
    }


    public String getClave() {
        return clave;
    }

    public String getConsecutivo() {
        return consecutivo;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getEmisorNumero() {
        return emisorNumero;
    }

    public String getReceptorNumero() {
        return receptorNumero;
    }

    public String getEmisorNombre() {
        return emisorNombre;
    }

    public String getReceptorNombre() {
        return receptorNombre;
    }

    public BigDecimal getTotalGravado() {
        return totalGravado;
    }

    public BigDecimal getTotalExento() {
        return totalExento;
    }

    public BigDecimal getTotalImpuesto() {
        return totalImpuesto;
    }

    public BigDecimal getTotalImpuestoDevuelto() {
        return totalImpuestoDevuelto;
    }

    public BigDecimal getTotalComprobante() {
        return totalComprobante;
    }

    public Boolean getHasBank() {
        return hasBank;
    }

    public Boolean getHasElectronic() {
        return hasElectronic;
    }

    public Boolean getHasPaper() {
        return hasPaper;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public int compareTo(RecordSummary that) {
        return new CompareToBuilder()
                .append(this.getFecha(), that.getFecha())
                .append(this.getCurrency().getCurrencyCode(), that.getCurrency().getCurrencyCode())
                .append(this.getTotalComprobante(), that.getTotalComprobante())
                .toComparison()
                ;
    }
}

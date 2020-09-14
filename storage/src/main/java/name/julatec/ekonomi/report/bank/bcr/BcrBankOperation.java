package name.julatec.ekonomi.report.bank.bcr;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvIgnore;
import name.julatec.ekonomi.report.bank.BankOperation;
import name.julatec.ekonomi.report.csv.DefaultSeparator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

@DefaultSeparator('\t')
public class BcrBankOperation implements BankOperation<BcrBankOperation> {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String LOCALE = "es_CR";

    @CsvDate(DATE_FORMAT)
    @CsvBindByName(column = "Fecha", format = DATE_FORMAT, locale = LOCALE)
    private Date date;

    @CsvBindByName(column = "Número", locale = LOCALE)
    private String documentNumber;

    @CsvBindByName(column = "Abono", locale = LOCALE)
    private BigDecimal payment;

    @CsvBindByName(column = "Intereses", locale = LOCALE)
    private BigDecimal interest;

    @CsvBindByName(column = "Polizas", locale = LOCALE)
    private BigDecimal insurance;

    @CsvBindByName(column = "Otros cobros", locale = LOCALE)
    private BigDecimal charges;

    @CsvBindByName(column = "Total Pagado", locale = LOCALE)
    private BigDecimal total;

    @CsvBindByName(column = "Nuevo Saldo", locale = LOCALE)
    private BigDecimal principal;

    @CsvIgnore
    private Currency currency;

    @Override
    public Date getDate() {
        return date;
    }

    public BcrBankOperation setDate(Date date) {
        this.date = date;
        return this;
    }

    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    public BcrBankOperation setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    @Override
    public BigDecimal getPayment() {
        return payment;
    }

    public BcrBankOperation setPayment(BigDecimal payment) {
        this.payment = payment;
        return this;
    }

    @Override
    public BigDecimal getInterest() {
        return interest;
    }

    public BcrBankOperation setInterest(BigDecimal interest) {
        this.interest = interest;
        return this;
    }

    public BigDecimal getInsurance() {
        return insurance;
    }

    public BcrBankOperation setInsurance(BigDecimal insurance) {
        this.insurance = insurance;
        return this;
    }

    public BigDecimal getCharges() {
        return charges;
    }

    public BcrBankOperation setCharges(BigDecimal charges) {
        this.charges = charges;
        return this;
    }

    @Override
    public BigDecimal getTotal() {
        return total;
    }

    public BcrBankOperation setTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }

    @Override
    public BcrBankOperation setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public BcrBankOperation setPrincipal(BigDecimal principal) {
        this.principal = principal;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("date", date)
                .append("documentNumber", documentNumber)
                .append("payment", payment)
                .append("interest", interest)
                .append("insurance", insurance)
                .append("charges", charges)
                .append("total", total)
                .append("principal", principal)
                .append("currency", currency)
                .toString();
    }
}

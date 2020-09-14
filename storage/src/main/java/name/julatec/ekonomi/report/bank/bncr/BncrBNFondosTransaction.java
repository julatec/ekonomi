package name.julatec.ekonomi.report.bank.bncr;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvIgnore;
import name.julatec.ekonomi.report.bank.BankTransaction;
import name.julatec.ekonomi.report.csv.DefaultSeparator;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

@DefaultSeparator(',')
public class BncrBNFondosTransaction implements BankTransaction<BncrBNFondosTransaction> {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String LOCALE = "es_CR";

    @CsvDate(DATE_FORMAT)
    @CsvBindByName(column = "Fecha", format = DATE_FORMAT, locale = LOCALE)
    private Date date;

    @CsvBindByName(column = "Tipo")
    private String type;

    @CsvBindByName(column = "numeroDocumento")
    private String numeroDocumento;

    @CsvBindByName(column = "Numero de participaciones", locale = LOCALE)
    private BigDecimal participations;

    @CsvBindByName(column = "Valor de participacion", locale = LOCALE)
    private BigDecimal participationValue;

    @CsvBindByName(column = "Monto", locale = LOCALE)
    private BigDecimal amount;

    @CsvBindByName(column = "Comprobante")
    private String documentNumber;

    @CsvIgnore
    private Currency currency;

    @CsvIgnore
    private String account;

    @Override
    public Date getDate() {
        return date;
    }

    public BncrBNFondosTransaction setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getType() {
        return type;
    }

    public BncrBNFondosTransaction setType(String type) {
        this.type = type;
        return this;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public BncrBNFondosTransaction setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
        return this;
    }

    public BigDecimal getParticipations() {
        return participations;
    }

    public BncrBNFondosTransaction setParticipations(BigDecimal participations) {
        this.participations = participations;
        return this;
    }

    public BigDecimal getParticipationValue() {
        return participationValue;
    }

    public BncrBNFondosTransaction setParticipationValue(BigDecimal participationValue) {
        this.participationValue = participationValue;
        return this;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    public BncrBNFondosTransaction setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    @Override
    public String getDescription() {
        return String.format("%s %s of %s", getType(), getDocumentNumber(), getAccount());
    }

    public BncrBNFondosTransaction setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }

    public BncrBNFondosTransaction setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    @Override
    public String getAccount() {
        return account;
    }

    public BncrBNFondosTransaction setAccount(String account) {
        this.account = account;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("date", date)
                .append("type", type)
                .append("numeroDocumento", numeroDocumento)
                .append("participations", participations)
                .append("participationValue", participationValue)
                .append("amount", amount)
                .append("documentNumber", documentNumber)
                .append("currency", currency)
                .append("account", account)
                .toString();
    }
}

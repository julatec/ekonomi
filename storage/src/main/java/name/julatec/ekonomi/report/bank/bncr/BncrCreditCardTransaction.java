package name.julatec.ekonomi.report.bank.bncr;

import com.opencsv.bean.*;
import name.julatec.ekonomi.report.bank.BankTransaction;
import name.julatec.ekonomi.report.csv.DefaultSeparator;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

@DefaultSeparator(',')
public class BncrCreditCardTransaction implements BankTransaction<BncrCreditCardTransaction> {

    private static final Map<String, Currency> CURRENCY_MAP = Map.of(
            Currency.getInstance("CRC").getCurrencyCode(), Currency.getInstance("CRC"),
            Currency.getInstance("USD").getCurrencyCode(), Currency.getInstance("USD"),
            "Colones", Currency.getInstance("CRC"),
            "Dólares", Currency.getInstance("USD"));

    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("CRC");

    @CsvBindByName(column = "Ref")
    private String documentNumber;

    @CsvDate("dd/MM/yyyy")
    @CsvBindByName(column = "Fecha", format = "dd/MM/yyyy")
    private Date date;

    @CsvBindByName(column = "Tipo")
    private String type;

    @CsvBindByName(column = "Establecimiento")
    private String description;

    @CsvBindByName(column = "Monto", locale = "es_CR")
    private BigDecimal amount;

    @CsvBindByName(column = "Moneda")
    private String currencyName;

    @CsvIgnore
    private String account;

    public static Stream<BncrCreditCardTransaction> stream(InputStream stream) {
        final InputStreamReader reader = new InputStreamReader(stream);
        CsvToBean<BncrCreditCardTransaction> csvToBean = new CsvToBeanBuilder(reader)
                .withType(BncrCreditCardTransaction.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withSeparator(',')
                .build();
        return csvToBean.stream();
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public BncrCreditCardTransaction setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public Currency getCurrency() {
        return CURRENCY_MAP.getOrDefault(getCurrencyName(), DEFAULT_CURRENCY);
    }

    @Override
    public BncrCreditCardTransaction setCurrency(Currency currency) {
        this.currencyName = currency.getCurrencyCode();
        return this;
    }

    public BncrCreditCardTransaction setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getType() {
        return type;
    }

    public BncrCreditCardTransaction setType(String type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public BncrCreditCardTransaction setDescription(String description) {
        this.description = description;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BncrCreditCardTransaction setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public BncrCreditCardTransaction setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
        return this;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public BncrCreditCardTransaction setAccount(String account) {
        this.account = account;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("documentNumber", documentNumber)
                .append("date", date)
                .append("type", type)
                .append("description", description)
                .append("amount", amount)
                .append("currencyName", currencyName)
                .append("account", account)
                .toString();
    }
}

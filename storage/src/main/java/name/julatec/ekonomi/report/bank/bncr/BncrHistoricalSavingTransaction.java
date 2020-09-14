package name.julatec.ekonomi.report.bank.bncr;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvIgnore;
import name.julatec.ekonomi.report.bank.BankTransaction;
import name.julatec.ekonomi.report.csv.CsvBindByNameOrder;
import name.julatec.ekonomi.report.csv.DefaultSeparator;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

@CsvBindByNameOrder({
        "Fecha",
        "Numero",
        "Descripcion",
        "Monto",
        "Saldo Diario",
})
@DefaultSeparator('\t')
public class BncrHistoricalSavingTransaction implements BankTransaction<BncrHistoricalSavingTransaction> {

    @CsvDate("dd/MM/yyyy")
    @CsvBindByName(column = "Fecha", format = "dd/MM/yyyy")
    private Date date;

    @CsvBindByName(column = "Numero")
    private String documentNumber;

    @CsvBindByName(column = "Descripcion")
    private String description;

    @CsvBindByName(column = "Monto", locale = "es_CR")
    private BigDecimal amount;

    @CsvBindByName(column = "Saldo Diario", locale = "es_CR")
    private BigDecimal dailyAmount;

    @CsvIgnore
    private String account;

    @CsvIgnore
    private Currency currency;

    @Override
    public Date getDate() {
        return date;
    }

    public BncrHistoricalSavingTransaction setDate(Date date) {
        this.date = date;
        return this;
    }

    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    public BncrHistoricalSavingTransaction setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public BncrHistoricalSavingTransaction setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    public BncrHistoricalSavingTransaction setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public BigDecimal getDailyAmount() {
        return dailyAmount;
    }

    public BncrHistoricalSavingTransaction setDailyAmount(BigDecimal dailyAmount) {
        this.dailyAmount = dailyAmount;
        return this;
    }

    @Override
    public String getAccount() {
        return account;
    }

    public BncrHistoricalSavingTransaction setAccount(String account) {
        this.account = account;
        return this;
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }

    public BncrHistoricalSavingTransaction setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("date", date)
                .append("documentNumber", documentNumber)
                .append("description", description)
                .append("amount", amount)
                .append("dailyAmount", dailyAmount)
                .append("account", account)
                .append("currency", currency)
                .toString();
    }
}

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

@DefaultSeparator(';')
public class BncrSavingTransaction implements BankTransaction<BncrSavingTransaction> {

    @CsvDate("dd/MM/yyyy")
    @CsvBindByName(column = "fechaMovimiento", format = "dd/MM/yyyy")
    private Date date;

    @CsvBindByName(column = "numeroDocumento")
    private String documentNumber;

    @CsvBindByName(column = "debito", locale = "es_CR")
    private BigDecimal debit;

    @CsvBindByName(column = "credito", locale = "es_CR")
    private BigDecimal credit;

    @CsvBindByName(column = "descripcion")
    private String description;

    @CsvIgnore
    private String account;

    @CsvIgnore
    private Currency currency;

    public Date getDate() {
        return date;
    }

    public BncrSavingTransaction setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public BncrSavingTransaction setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public BncrSavingTransaction setDebit(BigDecimal debit) {
        this.debit = debit;
        return this;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public BncrSavingTransaction setCredit(BigDecimal credit) {
        this.credit = credit;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public BncrSavingTransaction setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getAccount() {
        return account;
    }

    public BncrSavingTransaction setAccount(String account) {
        this.account = account;
        return this;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BncrSavingTransaction setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    @Override
    public BigDecimal getAmount() {
        return debit == null ? debit : credit.negate();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("date", date)
                .append("documentNumber", documentNumber)
                .append("debit", debit)
                .append("credit", credit)
                .append("description", description)
                .append("account", account)
                .append("currency", currency)
                .toString();
    }
}


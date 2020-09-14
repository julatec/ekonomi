package name.julatec.ekonomi.report.bank.bcr;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvIgnore;
import name.julatec.ekonomi.report.bank.BankTransaction;
import name.julatec.ekonomi.report.csv.DefaultSeparator;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

@DefaultSeparator('\t')
public class BcrSavingTransaction implements BankTransaction<BcrSavingTransaction> {

    public static final String DATE_FORMAT = "dd-MM-yy";
    public static final String LOCALE = "es_CR";

    @CsvDate(DATE_FORMAT)
    @CsvBindByName(column = "CONTABLE", format = DATE_FORMAT, locale = LOCALE)
    private Date accountingDate;

    @CsvDate(DATE_FORMAT)
    @CsvBindByName(column = "MOVIMIENTO", format = DATE_FORMAT, locale = LOCALE)
    private Date transactionDate;

    @CsvBindByName(column = "NUMERO", locale = LOCALE)
    private String documentNumber;

    @CsvBindByName(column = "DESCRIPCION", locale = LOCALE)
    private String description;

    @CsvBindByName(column = "OFICINA", locale = LOCALE)
    private String branch;

    @CsvBindByName(column = "DEBITOS", locale = LOCALE)
    private BigDecimal debit;

    @CsvBindByName(column = "CREDITOS", locale = LOCALE)
    private BigDecimal credit;

    @CsvIgnore
    private Currency currency;

    @CsvIgnore
    private String account;

    @Override
    public Date getDate() {
        return transactionDate;
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }

    @Override
    public BcrSavingTransaction setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    @Override
    public BigDecimal getAmount() {
        return debit != null ? debit.negate() : credit;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public BcrSavingTransaction setAccount(String account) {
        this.account = account;
        return this;
    }

    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    public BcrSavingTransaction setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public BcrSavingTransaction setDescription(String description) {
        this.description = description;
        return this;
    }

    public Date getAccountingDate() {
        return accountingDate;
    }

    public BcrSavingTransaction setAccountingDate(Date accountingDate) {
        this.accountingDate = accountingDate;
        return this;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public BcrSavingTransaction setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

    public String getBranch() {
        return branch;
    }

    public BcrSavingTransaction setBranch(String branch) {
        this.branch = branch;
        return this;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public BcrSavingTransaction setDebit(BigDecimal debit) {
        this.debit = debit;
        return this;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public BcrSavingTransaction setCredit(BigDecimal credit) {
        this.credit = credit;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("accountingDate", accountingDate)
                .append("transactionDate", transactionDate)
                .append("documentNumber", documentNumber)
                .append("description", description)
                .append("branch", branch)
                .append("debit", debit)
                .append("credit", credit)
                .append("currency", currency)
                .append("account", account)
                .toString();
    }
}
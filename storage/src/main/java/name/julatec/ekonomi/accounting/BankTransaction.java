package name.julatec.ekonomi.accounting;


import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.UUID;

import static java.lang.Long.toHexString;
import static java.lang.String.format;
import static name.julatec.ekonomi.tribunet.UuidFormatter.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity(name = "bank_transaction")
public class BankTransaction implements
        name.julatec.ekonomi.report.bank.BankTransaction<BankTransaction>,
        RecordComparable,
        Comparable<BankTransaction> {

    public static final String EXPORT_NAME = "bankVoucher";

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    private String guid;

    private Date date;

    private String documentNumber;

    private String description;

    private BigDecimal amount;

    private String account;

    private Currency currency;

    @Transient
    private final Record.Key key = new Record.Key(
            this::getDate,
            () -> getAmount().abs(),
            this::getCurrency);

    @Override
    public Date getDate() {
        return date;
    }

    public BankTransaction setDate(Date date) {
        this.date = date;
        return this;
    }

    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    public BankTransaction setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public BankTransaction setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    public BankTransaction setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public BankTransaction setAccount(String account) {
        this.account = account;
        return this;
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }

    @Override
    public BankTransaction setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    @Override
    public UUID getId() {
        return uuidFromString(format("%s%s%s",
                toHexString(getDate().getTime()),
                Integer.toHexString(String.valueOf(getDocumentNumber()).hashCode()),
                Integer.toHexString(String.valueOf(getAccount()).hashCode())
        ));
    }

    public BankTransaction setId(UUID uuid) {
        this.guid = uuidToString(uuid);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("guid", guid)
                .append("date", date)
                .append("documentNumber", documentNumber)
                .append("description", description)
                .append("amount", amount)
                .append("account", account)
                .append("currency", currency)
                .toString();
    }

    @Override
    public Record.Key getKey() {
        return key;
    }

    @Override
    public int compareTo(BankTransaction that) {
        return key.compareTo(that.key);
    }

    public static BankTransaction of(name.julatec.ekonomi.report.bank.BankTransaction<? extends name.julatec.ekonomi.report.bank.BankTransaction> bankTransaction) {
        return new BankTransaction()
                .setAccount(bankTransaction.getAccount())
                .setAmount(bankTransaction.getAmount())
                .setCurrency(bankTransaction.getCurrency())
                .setDate(bankTransaction.getDate())
                .setDocumentNumber(bankTransaction.getDocumentNumber())
                .setDescription(bankTransaction.getDescription())
                .setId(bankTransaction.getId());
    }
}
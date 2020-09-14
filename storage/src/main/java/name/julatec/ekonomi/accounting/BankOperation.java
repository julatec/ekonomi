package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.tribunet.UuidFormatter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.UUID;

import static java.lang.Long.toHexString;
import static java.lang.String.format;
import static name.julatec.ekonomi.accounting.EmbeddedTransaction.*;
import static name.julatec.ekonomi.tribunet.UuidFormatter.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity(name = "bank_operation")
public class BankOperation implements
        RecordComparable,
        Comparable<BankOperation> {

    public static final String PAYMENT = "Payment";
    public static final String INTEREST = "Interest";
    public static final String CHARGES = "Charges";
    public static final String INSURANCE = "Insurance";
    public static final String EXPORT_NAME = "bankoperation";
    public static final String EXPORT_NAME_PAYMENT = "bankoperationpayment";

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    private String guid;

    private Date date;

    private String documentNumber;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = ACCOUNT, column = @Column(name = ACCOUNT + PAYMENT)),
            @AttributeOverride(name = AMOUNT, column = @Column(name = AMOUNT + PAYMENT))
    })
    private EmbeddedTransaction payment;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = ACCOUNT, column = @Column(name = ACCOUNT + INTEREST)),
            @AttributeOverride(name = AMOUNT, column = @Column(name = AMOUNT + INTEREST))
    })
    private EmbeddedTransaction interest;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = ACCOUNT, column = @Column(name = ACCOUNT + INSURANCE)),
            @AttributeOverride(name = AMOUNT, column = @Column(name = AMOUNT + INSURANCE))
    })
    private EmbeddedTransaction insurance;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = ACCOUNT, column = @Column(name = ACCOUNT + CHARGES)),
            @AttributeOverride(name = AMOUNT, column = @Column(name = AMOUNT + CHARGES))
    })
    private EmbeddedTransaction charges;

    private BigDecimal total;

    private BigDecimal principal;

    private Currency currency;

    @Transient
    private final Record.Key key = new Record.Key(
            this::getDate,
            () -> getTotal().abs(),
            this::getCurrency);

    public Date getDate() {
        return date;
    }

    public BankOperation setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public BankOperation setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public EmbeddedTransaction getPayment() {
        return payment;
    }

    public BankOperation setPayment(EmbeddedTransaction payment) {
        this.payment = payment;
        return this;
    }

    public EmbeddedTransaction getInterest() {
        return interest;
    }

    public BankOperation setInterest(EmbeddedTransaction interest) {
        this.interest = interest;
        return this;
    }

    public EmbeddedTransaction getInsurance() {
        return insurance;
    }

    public BankOperation setInsurance(EmbeddedTransaction insurance) {
        this.insurance = insurance;
        return this;
    }

    public EmbeddedTransaction getCharges() {
        return charges;
    }

    public BankOperation setCharges(EmbeddedTransaction charges) {
        this.charges = charges;
        return this;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BankOperation setTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public BankOperation setPrincipal(BigDecimal principal) {
        this.principal = principal;
        return this;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BankOperation setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public Record.Key getKey() {
        return key;
    }

    public UUID getId() {
        if (this.guid == null) {
            return uuidFromString(format("%s%s%s",
                    toHexString(getDate().getTime()),
                    Integer.toHexString(String.valueOf(getDocumentNumber()).hashCode()),
                    Integer.toHexString(getPayment().getAccount().hashCode())
            ));
        }
        return UuidFormatter.uuidFromString(guid);
    }

    public BankOperation setId(UUID uuid) {
        this.guid = uuidToString(uuid);
        return this;
    }

    @Override
    public int compareTo(BankOperation that) {
        return key.compareTo(that.key);
    }
}

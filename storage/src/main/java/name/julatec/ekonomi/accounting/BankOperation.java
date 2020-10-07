package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.storage.UuidConverter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.UUID;

import static java.lang.Long.toHexString;
import static java.lang.String.format;
import static name.julatec.ekonomi.accounting.EmbeddedTransaction.ACCOUNT;
import static name.julatec.ekonomi.accounting.EmbeddedTransaction.AMOUNT;
import static name.julatec.ekonomi.tribunet.UuidFormatter.uuidFromString;

/**
 * Bank operation records contains the data of single payment that is the composition of multiple entries.
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity(name = BankOperation.EXPORT_NAME)
public class BankOperation implements
        RecordComparable,
        Comparable<BankOperation> {

    public static final String PAYMENT = "Payment";
    public static final String INTEREST = "Interest";
    public static final String CHARGES = "Charges";
    public static final String INSURANCE = "Insurance";
    public static final String EXPORT_NAME = "bank_operation";
    public static final String EXPORT_NAME_PAYMENT = "bankoperationpayment";

    /**
     * Unique identifier.
     */
    @Id
    @Column(name = "guid", nullable = false, length = 32)
    @Convert(converter = UuidConverter.class)
    private UUID guid;

    /**
     * Date of the operation.
     */
    private Date date;

    /**
     * Document number, ussually the bank identifier.
     */
    private String documentNumber;

    /**
     * Payment to the principal.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = ACCOUNT, column = @Column(name = ACCOUNT + PAYMENT)),
            @AttributeOverride(name = AMOUNT, column = @Column(name = AMOUNT + PAYMENT))
    })
    private EmbeddedTransaction payment;

    /**
     * Insterest of the operation.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = ACCOUNT, column = @Column(name = ACCOUNT + INTEREST)),
            @AttributeOverride(name = AMOUNT, column = @Column(name = AMOUNT + INTEREST))
    })
    private EmbeddedTransaction interest;

    /**
     * Insurance.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = ACCOUNT, column = @Column(name = ACCOUNT + INSURANCE)),
            @AttributeOverride(name = AMOUNT, column = @Column(name = AMOUNT + INSURANCE))
    })
    private EmbeddedTransaction insurance;

    /**
     * Any other charges.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = ACCOUNT, column = @Column(name = ACCOUNT + CHARGES)),
            @AttributeOverride(name = AMOUNT, column = @Column(name = AMOUNT + CHARGES))
    })
    private EmbeddedTransaction charges;

    /**
     * Transaction total
     */
    private BigDecimal total;

    /**
     * Reaming principal.
     */
    private BigDecimal principal;

    /**
     * Currency.
     */
    private Currency currency;

    /**
     * A record ky used to identify and sort this operation.
     */
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
        return guid;
    }

    public BankOperation setId(UUID uuid) {
        this.guid = uuid;
        return this;
    }

    @Override
    public int compareTo(BankOperation that) {
        return key.compareTo(that.key);
    }
}

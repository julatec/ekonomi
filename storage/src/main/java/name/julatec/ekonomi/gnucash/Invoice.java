package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.sql.Timestamp;
import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "invoices")
public class Invoice {
    private String guid;
    private String id;
    private Timestamp dateOpened;
    private Timestamp datePosted;
    private String notes;
    private int active;
    private String currency;
    private Integer ownerType;
    private String ownerGuid;
    private String terms;
    private String billingId;
    private String postTxn;
    private String postLot;
    private String postAcc;
    private Integer billtoType;
    private String billtoGuid;
    private Long chargeAmtNum;
    private Long chargeAmtDenom;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Invoice setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "id", nullable = false, length = 2048)
    public String getId() {
        return id;
    }

    public Invoice setId(String id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "date_opened", nullable = true)
    public Timestamp getDateOpened() {
        return dateOpened;
    }

    public Invoice setDateOpened(Timestamp dateOpened) {
        this.dateOpened = dateOpened;
        return this;
    }

    @Basic
    @Column(name = "date_posted", nullable = true)
    public Timestamp getDatePosted() {
        return datePosted;
    }

    public Invoice setDatePosted(Timestamp datePosted) {
        this.datePosted = datePosted;
        return this;
    }

    @Basic
    @Column(name = "notes", nullable = false, length = 2048)
    public String getNotes() {
        return notes;
    }

    public Invoice setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    @Basic
    @Column(name = "active", nullable = false)
    public int getActive() {
        return active;
    }

    public Invoice setActive(int active) {
        this.active = active;
        return this;
    }

    @Basic
    @Column(name = "currency", nullable = false, length = 32)
    public String getCurrency() {
        return currency;
    }

    public Invoice setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    @Basic
    @Column(name = "owner_type", nullable = true)
    public Integer getOwnerType() {
        return ownerType;
    }

    public Invoice setOwnerType(Integer ownerType) {
        this.ownerType = ownerType;
        return this;
    }

    @Basic
    @Column(name = "owner_guid", nullable = true, length = 32)
    public String getOwnerGuid() {
        return ownerGuid;
    }

    public Invoice setOwnerGuid(String ownerGuid) {
        this.ownerGuid = ownerGuid;
        return this;
    }

    @Basic
    @Column(name = "terms", nullable = true, length = 32)
    public String getTerms() {
        return terms;
    }

    public Invoice setTerms(String terms) {
        this.terms = terms;
        return this;
    }

    @Basic
    @Column(name = "billing_id", nullable = true, length = 2048)
    public String getBillingId() {
        return billingId;
    }

    public Invoice setBillingId(String billingId) {
        this.billingId = billingId;
        return this;
    }

    @Basic
    @Column(name = "post_txn", nullable = true, length = 32)
    public String getPostTxn() {
        return postTxn;
    }

    public Invoice setPostTxn(String postTxn) {
        this.postTxn = postTxn;
        return this;
    }

    @Basic
    @Column(name = "post_lot", nullable = true, length = 32)
    public String getPostLot() {
        return postLot;
    }

    public Invoice setPostLot(String postLot) {
        this.postLot = postLot;
        return this;
    }

    @Basic
    @Column(name = "post_acc", nullable = true, length = 32)
    public String getPostAcc() {
        return postAcc;
    }

    public Invoice setPostAcc(String postAcc) {
        this.postAcc = postAcc;
        return this;
    }

    @Basic
    @Column(name = "billto_type", nullable = true)
    public Integer getBilltoType() {
        return billtoType;
    }

    public Invoice setBilltoType(Integer billtoType) {
        this.billtoType = billtoType;
        return this;
    }

    @Basic
    @Column(name = "billto_guid", nullable = true, length = 32)
    public String getBilltoGuid() {
        return billtoGuid;
    }

    public Invoice setBilltoGuid(String billtoGuid) {
        this.billtoGuid = billtoGuid;
        return this;
    }

    @Basic
    @Column(name = "charge_amt_num", nullable = true)
    public Long getChargeAmtNum() {
        return chargeAmtNum;
    }

    public Invoice setChargeAmtNum(Long chargeAmtNum) {
        this.chargeAmtNum = chargeAmtNum;
        return this;
    }

    @Basic
    @Column(name = "charge_amt_denom", nullable = true)
    public Long getChargeAmtDenom() {
        return chargeAmtDenom;
    }

    public Invoice setChargeAmtDenom(Long chargeAmtDenom) {
        this.chargeAmtDenom = chargeAmtDenom;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Invoice invoice = (Invoice) o;

        return new EqualsBuilder()
                .append(active, invoice.active)
                .append(guid, invoice.guid)
                .append(id, invoice.id)
                .append(dateOpened, invoice.dateOpened)
                .append(datePosted, invoice.datePosted)
                .append(notes, invoice.notes)
                .append(currency, invoice.currency)
                .append(ownerType, invoice.ownerType)
                .append(ownerGuid, invoice.ownerGuid)
                .append(terms, invoice.terms)
                .append(billingId, invoice.billingId)
                .append(postTxn, invoice.postTxn)
                .append(postLot, invoice.postLot)
                .append(postAcc, invoice.postAcc)
                .append(billtoType, invoice.billtoType)
                .append(billtoGuid, invoice.billtoGuid)
                .append(chargeAmtNum, invoice.chargeAmtNum)
                .append(chargeAmtDenom, invoice.chargeAmtDenom)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(id)
                .append(dateOpened)
                .append(datePosted)
                .append(notes)
                .append(active)
                .append(currency)
                .append(ownerType)
                .append(ownerGuid)
                .append(terms)
                .append(billingId)
                .append(postTxn)
                .append(postLot)
                .append(postAcc)
                .append(billtoType)
                .append(billtoGuid)
                .append(chargeAmtNum)
                .append(chargeAmtDenom)
                .toHashCode();
    }
}

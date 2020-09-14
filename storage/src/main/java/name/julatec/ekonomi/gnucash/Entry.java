package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "entries")
public class Entry {
    private String guid;
    private Timestamp date;
    private Timestamp dateEntered;
    private String description;
    private String action;
    private String notes;
    private Long quantityNum;
    private Long quantityDenom;
    private String iAcct;
    private Long iPriceNum;
    private Long iPriceDenom;
    private Long iDiscountNum;
    private Long iDiscountDenom;
    private String invoice;
    private String iDiscType;
    private String iDiscHow;
    private Integer iTaxable;
    private Integer iTaxincluded;
    private String iTaxtable;
    private String bAcct;
    private Long bPriceNum;
    private Long bPriceDenom;
    private String bill;
    private Integer bTaxable;
    private Integer bTaxincluded;
    private String bTaxtable;
    private Integer bPaytype;
    private Integer billable;
    private Integer billtoType;
    private String billtoGuid;
    private String orderGuid;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Entry setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "date", nullable = false)
    public Timestamp getDate() {
        return date;
    }

    public Entry setDate(Timestamp date) {
        this.date = date;
        return this;
    }

    @Basic
    @Column(name = "date_entered", nullable = true)
    public Timestamp getDateEntered() {
        return dateEntered;
    }

    public Entry setDateEntered(Timestamp dateEntered) {
        this.dateEntered = dateEntered;
        return this;
    }

    @Basic
    @Column(name = "description", nullable = true, length = 2048)
    public String getDescription() {
        return description;
    }

    public Entry setDescription(String description) {
        this.description = description;
        return this;
    }

    @Basic
    @Column(name = "action", nullable = true, length = 2048)
    public String getAction() {
        return action;
    }

    public Entry setAction(String action) {
        this.action = action;
        return this;
    }

    @Basic
    @Column(name = "notes", nullable = true, length = 2048)
    public String getNotes() {
        return notes;
    }

    public Entry setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    @Basic
    @Column(name = "quantity_num", nullable = true)
    public Long getQuantityNum() {
        return quantityNum;
    }

    public Entry setQuantityNum(Long quantityNum) {
        this.quantityNum = quantityNum;
        return this;
    }

    @Basic
    @Column(name = "quantity_denom", nullable = true)
    public Long getQuantityDenom() {
        return quantityDenom;
    }

    public Entry setQuantityDenom(Long quantityDenom) {
        this.quantityDenom = quantityDenom;
        return this;
    }

    @Basic
    @Column(name = "i_acct", nullable = true, length = 32)
    public String getiAcct() {
        return iAcct;
    }

    public Entry setiAcct(String iAcct) {
        this.iAcct = iAcct;
        return this;
    }

    @Basic
    @Column(name = "i_price_num", nullable = true)
    public Long getiPriceNum() {
        return iPriceNum;
    }

    public Entry setiPriceNum(Long iPriceNum) {
        this.iPriceNum = iPriceNum;
        return this;
    }

    @Basic
    @Column(name = "i_price_denom", nullable = true)
    public Long getiPriceDenom() {
        return iPriceDenom;
    }

    public Entry setiPriceDenom(Long iPriceDenom) {
        this.iPriceDenom = iPriceDenom;
        return this;
    }

    @Basic
    @Column(name = "i_discount_num", nullable = true)
    public Long getiDiscountNum() {
        return iDiscountNum;
    }

    public Entry setiDiscountNum(Long iDiscountNum) {
        this.iDiscountNum = iDiscountNum;
        return this;
    }

    @Basic
    @Column(name = "i_discount_denom", nullable = true)
    public Long getiDiscountDenom() {
        return iDiscountDenom;
    }

    public Entry setiDiscountDenom(Long iDiscountDenom) {
        this.iDiscountDenom = iDiscountDenom;
        return this;
    }

    @Basic
    @Column(name = "invoice", nullable = true, length = 32)
    public String getInvoice() {
        return invoice;
    }

    public Entry setInvoice(String invoice) {
        this.invoice = invoice;
        return this;
    }

    @Basic
    @Column(name = "i_disc_type", nullable = true, length = 2048)
    public String getiDiscType() {
        return iDiscType;
    }

    public Entry setiDiscType(String iDiscType) {
        this.iDiscType = iDiscType;
        return this;
    }

    @Basic
    @Column(name = "i_disc_how", nullable = true, length = 2048)
    public String getiDiscHow() {
        return iDiscHow;
    }

    public Entry setiDiscHow(String iDiscHow) {
        this.iDiscHow = iDiscHow;
        return this;
    }

    @Basic
    @Column(name = "i_taxable", nullable = true)
    public Integer getiTaxable() {
        return iTaxable;
    }

    public Entry setiTaxable(Integer iTaxable) {
        this.iTaxable = iTaxable;
        return this;
    }

    @Basic
    @Column(name = "i_taxincluded", nullable = true)
    public Integer getiTaxincluded() {
        return iTaxincluded;
    }

    public Entry setiTaxincluded(Integer iTaxincluded) {
        this.iTaxincluded = iTaxincluded;
        return this;
    }

    @Basic
    @Column(name = "i_taxtable", nullable = true, length = 32)
    public String getiTaxtable() {
        return iTaxtable;
    }

    public Entry setiTaxtable(String iTaxtable) {
        this.iTaxtable = iTaxtable;
        return this;
    }

    @Basic
    @Column(name = "b_acct", nullable = true, length = 32)
    public String getbAcct() {
        return bAcct;
    }

    public Entry setbAcct(String bAcct) {
        this.bAcct = bAcct;
        return this;
    }

    @Basic
    @Column(name = "b_price_num", nullable = true)
    public Long getbPriceNum() {
        return bPriceNum;
    }

    public Entry setbPriceNum(Long bPriceNum) {
        this.bPriceNum = bPriceNum;
        return this;
    }

    @Basic
    @Column(name = "b_price_denom", nullable = true)
    public Long getbPriceDenom() {
        return bPriceDenom;
    }

    public Entry setbPriceDenom(Long bPriceDenom) {
        this.bPriceDenom = bPriceDenom;
        return this;
    }

    @Basic
    @Column(name = "bill", nullable = true, length = 32)
    public String getBill() {
        return bill;
    }

    public Entry setBill(String bill) {
        this.bill = bill;
        return this;
    }

    @Basic
    @Column(name = "b_taxable", nullable = true)
    public Integer getbTaxable() {
        return bTaxable;
    }

    public Entry setbTaxable(Integer bTaxable) {
        this.bTaxable = bTaxable;
        return this;
    }

    @Basic
    @Column(name = "b_taxincluded", nullable = true)
    public Integer getbTaxincluded() {
        return bTaxincluded;
    }

    public Entry setbTaxincluded(Integer bTaxincluded) {
        this.bTaxincluded = bTaxincluded;
        return this;
    }

    @Basic
    @Column(name = "b_taxtable", nullable = true, length = 32)
    public String getbTaxtable() {
        return bTaxtable;
    }

    public Entry setbTaxtable(String bTaxtable) {
        this.bTaxtable = bTaxtable;
        return this;
    }

    @Basic
    @Column(name = "b_paytype", nullable = true)
    public Integer getbPaytype() {
        return bPaytype;
    }

    public Entry setbPaytype(Integer bPaytype) {
        this.bPaytype = bPaytype;
        return this;
    }

    @Basic
    @Column(name = "billable", nullable = true)
    public Integer getBillable() {
        return billable;
    }

    public Entry setBillable(Integer billable) {
        this.billable = billable;
        return this;
    }

    @Basic
    @Column(name = "billto_type", nullable = true)
    public Integer getBilltoType() {
        return billtoType;
    }

    public Entry setBilltoType(Integer billtoType) {
        this.billtoType = billtoType;
        return this;
    }

    @Basic
    @Column(name = "billto_guid", nullable = true, length = 32)
    public String getBilltoGuid() {
        return billtoGuid;
    }

    public Entry setBilltoGuid(String billtoGuid) {
        this.billtoGuid = billtoGuid;
        return this;
    }

    @Basic
    @Column(name = "order_guid", nullable = true, length = 32)
    public String getOrderGuid() {
        return orderGuid;
    }

    public Entry setOrderGuid(String orderGuid) {
        this.orderGuid = orderGuid;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Entry entry = (Entry) o;

        return new EqualsBuilder()
                .append(guid, entry.guid)
                .append(date, entry.date)
                .append(dateEntered, entry.dateEntered)
                .append(description, entry.description)
                .append(action, entry.action)
                .append(notes, entry.notes)
                .append(quantityNum, entry.quantityNum)
                .append(quantityDenom, entry.quantityDenom)
                .append(iAcct, entry.iAcct)
                .append(iPriceNum, entry.iPriceNum)
                .append(iPriceDenom, entry.iPriceDenom)
                .append(iDiscountNum, entry.iDiscountNum)
                .append(iDiscountDenom, entry.iDiscountDenom)
                .append(invoice, entry.invoice)
                .append(iDiscType, entry.iDiscType)
                .append(iDiscHow, entry.iDiscHow)
                .append(iTaxable, entry.iTaxable)
                .append(iTaxincluded, entry.iTaxincluded)
                .append(iTaxtable, entry.iTaxtable)
                .append(bAcct, entry.bAcct)
                .append(bPriceNum, entry.bPriceNum)
                .append(bPriceDenom, entry.bPriceDenom)
                .append(bill, entry.bill)
                .append(bTaxable, entry.bTaxable)
                .append(bTaxincluded, entry.bTaxincluded)
                .append(bTaxtable, entry.bTaxtable)
                .append(bPaytype, entry.bPaytype)
                .append(billable, entry.billable)
                .append(billtoType, entry.billtoType)
                .append(billtoGuid, entry.billtoGuid)
                .append(orderGuid, entry.orderGuid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(date)
                .append(dateEntered)
                .append(description)
                .append(action)
                .append(notes)
                .append(quantityNum)
                .append(quantityDenom)
                .append(iAcct)
                .append(iPriceNum)
                .append(iPriceDenom)
                .append(iDiscountNum)
                .append(iDiscountDenom)
                .append(invoice)
                .append(iDiscType)
                .append(iDiscHow)
                .append(iTaxable)
                .append(iTaxincluded)
                .append(iTaxtable)
                .append(bAcct)
                .append(bPriceNum)
                .append(bPriceDenom)
                .append(bill)
                .append(bTaxable)
                .append(bTaxincluded)
                .append(bTaxtable)
                .append(bPaytype)
                .append(billable)
                .append(billtoType)
                .append(billtoGuid)
                .append(orderGuid)
                .toHashCode();
    }
}

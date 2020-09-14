package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {
    private String guid;
    private String name;
    private String id;
    private String notes;
    private int active;
    private long discountNum;
    private long discountDenom;
    private long creditNum;
    private long creditDenom;
    private String currency;
    private int taxOverride;
    private String addrName;
    private String addrAddr1;
    private String addrAddr2;
    private String addrAddr3;
    private String addrAddr4;
    private String addrPhone;
    private String addrFax;
    private String addrEmail;
    private String shipaddrName;
    private String shipaddrAddr1;
    private String shipaddrAddr2;
    private String shipaddrAddr3;
    private String shipaddrAddr4;
    private String shipaddrPhone;
    private String shipaddrFax;
    private String shipaddrEmail;
    private String terms;
    private Integer taxIncluded;
    private String taxtable;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Customer setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 2048)
    public String getName() {
        return name;
    }

    public Customer setName(String name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "id", nullable = false, length = 2048)
    public String getId() {
        return id;
    }

    public Customer setId(String id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "notes", nullable = false, length = 2048)
    public String getNotes() {
        return notes;
    }

    public Customer setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    @Basic
    @Column(name = "active", nullable = false)
    public int getActive() {
        return active;
    }

    public Customer setActive(int active) {
        this.active = active;
        return this;
    }

    @Basic
    @Column(name = "discount_num", nullable = false)
    public long getDiscountNum() {
        return discountNum;
    }

    public Customer setDiscountNum(long discountNum) {
        this.discountNum = discountNum;
        return this;
    }

    @Basic
    @Column(name = "discount_denom", nullable = false)
    public long getDiscountDenom() {
        return discountDenom;
    }

    public Customer setDiscountDenom(long discountDenom) {
        this.discountDenom = discountDenom;
        return this;
    }

    @Basic
    @Column(name = "credit_num", nullable = false)
    public long getCreditNum() {
        return creditNum;
    }

    public Customer setCreditNum(long creditNum) {
        this.creditNum = creditNum;
        return this;
    }

    @Basic
    @Column(name = "credit_denom", nullable = false)
    public long getCreditDenom() {
        return creditDenom;
    }

    public Customer setCreditDenom(long creditDenom) {
        this.creditDenom = creditDenom;
        return this;
    }

    @Basic
    @Column(name = "currency", nullable = false, length = 32)
    public String getCurrency() {
        return currency;
    }

    public Customer setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    @Basic
    @Column(name = "tax_override", nullable = false)
    public int getTaxOverride() {
        return taxOverride;
    }

    public Customer setTaxOverride(int taxOverride) {
        this.taxOverride = taxOverride;
        return this;
    }

    @Basic
    @Column(name = "addr_name", nullable = true, length = 1024)
    public String getAddrName() {
        return addrName;
    }

    public Customer setAddrName(String addrName) {
        this.addrName = addrName;
        return this;
    }

    @Basic
    @Column(name = "addr_addr1", nullable = true, length = 1024)
    public String getAddrAddr1() {
        return addrAddr1;
    }

    public Customer setAddrAddr1(String addrAddr1) {
        this.addrAddr1 = addrAddr1;
        return this;
    }

    @Basic
    @Column(name = "addr_addr2", nullable = true, length = 1024)
    public String getAddrAddr2() {
        return addrAddr2;
    }

    public Customer setAddrAddr2(String addrAddr2) {
        this.addrAddr2 = addrAddr2;
        return this;
    }

    @Basic
    @Column(name = "addr_addr3", nullable = true, length = 1024)
    public String getAddrAddr3() {
        return addrAddr3;
    }

    public Customer setAddrAddr3(String addrAddr3) {
        this.addrAddr3 = addrAddr3;
        return this;
    }

    @Basic
    @Column(name = "addr_addr4", nullable = true, length = 1024)
    public String getAddrAddr4() {
        return addrAddr4;
    }

    public Customer setAddrAddr4(String addrAddr4) {
        this.addrAddr4 = addrAddr4;
        return this;
    }

    @Basic
    @Column(name = "addr_phone", nullable = true, length = 128)
    public String getAddrPhone() {
        return addrPhone;
    }

    public Customer setAddrPhone(String addrPhone) {
        this.addrPhone = addrPhone;
        return this;
    }

    @Basic
    @Column(name = "addr_fax", nullable = true, length = 128)
    public String getAddrFax() {
        return addrFax;
    }

    public Customer setAddrFax(String addrFax) {
        this.addrFax = addrFax;
        return this;
    }

    @Basic
    @Column(name = "addr_email", nullable = true, length = 256)
    public String getAddrEmail() {
        return addrEmail;
    }

    public Customer setAddrEmail(String addrEmail) {
        this.addrEmail = addrEmail;
        return this;
    }

    @Basic
    @Column(name = "shipaddr_name", nullable = true, length = 1024)
    public String getShipaddrName() {
        return shipaddrName;
    }

    public Customer setShipaddrName(String shipaddrName) {
        this.shipaddrName = shipaddrName;
        return this;
    }

    @Basic
    @Column(name = "shipaddr_addr1", nullable = true, length = 1024)
    public String getShipaddrAddr1() {
        return shipaddrAddr1;
    }

    public Customer setShipaddrAddr1(String shipaddrAddr1) {
        this.shipaddrAddr1 = shipaddrAddr1;
        return this;
    }

    @Basic
    @Column(name = "shipaddr_addr2", nullable = true, length = 1024)
    public String getShipaddrAddr2() {
        return shipaddrAddr2;
    }

    public Customer setShipaddrAddr2(String shipaddrAddr2) {
        this.shipaddrAddr2 = shipaddrAddr2;
        return this;
    }

    @Basic
    @Column(name = "shipaddr_addr3", nullable = true, length = 1024)
    public String getShipaddrAddr3() {
        return shipaddrAddr3;
    }

    public Customer setShipaddrAddr3(String shipaddrAddr3) {
        this.shipaddrAddr3 = shipaddrAddr3;
        return this;
    }

    @Basic
    @Column(name = "shipaddr_addr4", nullable = true, length = 1024)
    public String getShipaddrAddr4() {
        return shipaddrAddr4;
    }

    public Customer setShipaddrAddr4(String shipaddrAddr4) {
        this.shipaddrAddr4 = shipaddrAddr4;
        return this;
    }

    @Basic
    @Column(name = "shipaddr_phone", nullable = true, length = 128)
    public String getShipaddrPhone() {
        return shipaddrPhone;
    }

    public Customer setShipaddrPhone(String shipaddrPhone) {
        this.shipaddrPhone = shipaddrPhone;
        return this;
    }

    @Basic
    @Column(name = "shipaddr_fax", nullable = true, length = 128)
    public String getShipaddrFax() {
        return shipaddrFax;
    }

    public Customer setShipaddrFax(String shipaddrFax) {
        this.shipaddrFax = shipaddrFax;
        return this;
    }

    @Basic
    @Column(name = "shipaddr_email", nullable = true, length = 256)
    public String getShipaddrEmail() {
        return shipaddrEmail;
    }

    public Customer setShipaddrEmail(String shipaddrEmail) {
        this.shipaddrEmail = shipaddrEmail;
        return this;
    }

    @Basic
    @Column(name = "terms", nullable = true, length = 32)
    public String getTerms() {
        return terms;
    }

    public Customer setTerms(String terms) {
        this.terms = terms;
        return this;
    }

    @Basic
    @Column(name = "tax_included", nullable = true)
    public Integer getTaxIncluded() {
        return taxIncluded;
    }

    public Customer setTaxIncluded(Integer taxIncluded) {
        this.taxIncluded = taxIncluded;
        return this;
    }

    @Basic
    @Column(name = "taxtable", nullable = true, length = 32)
    public String getTaxtable() {
        return taxtable;
    }

    public Customer setTaxtable(String taxtable) {
        this.taxtable = taxtable;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Customer customer = (Customer) o;

        return new EqualsBuilder()
                .append(active, customer.active)
                .append(discountNum, customer.discountNum)
                .append(discountDenom, customer.discountDenom)
                .append(creditNum, customer.creditNum)
                .append(creditDenom, customer.creditDenom)
                .append(taxOverride, customer.taxOverride)
                .append(guid, customer.guid)
                .append(name, customer.name)
                .append(id, customer.id)
                .append(notes, customer.notes)
                .append(currency, customer.currency)
                .append(addrName, customer.addrName)
                .append(addrAddr1, customer.addrAddr1)
                .append(addrAddr2, customer.addrAddr2)
                .append(addrAddr3, customer.addrAddr3)
                .append(addrAddr4, customer.addrAddr4)
                .append(addrPhone, customer.addrPhone)
                .append(addrFax, customer.addrFax)
                .append(addrEmail, customer.addrEmail)
                .append(shipaddrName, customer.shipaddrName)
                .append(shipaddrAddr1, customer.shipaddrAddr1)
                .append(shipaddrAddr2, customer.shipaddrAddr2)
                .append(shipaddrAddr3, customer.shipaddrAddr3)
                .append(shipaddrAddr4, customer.shipaddrAddr4)
                .append(shipaddrPhone, customer.shipaddrPhone)
                .append(shipaddrFax, customer.shipaddrFax)
                .append(shipaddrEmail, customer.shipaddrEmail)
                .append(terms, customer.terms)
                .append(taxIncluded, customer.taxIncluded)
                .append(taxtable, customer.taxtable)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(name)
                .append(id)
                .append(notes)
                .append(active)
                .append(discountNum)
                .append(discountDenom)
                .append(creditNum)
                .append(creditDenom)
                .append(currency)
                .append(taxOverride)
                .append(addrName)
                .append(addrAddr1)
                .append(addrAddr2)
                .append(addrAddr3)
                .append(addrAddr4)
                .append(addrPhone)
                .append(addrFax)
                .append(addrEmail)
                .append(shipaddrName)
                .append(shipaddrAddr1)
                .append(shipaddrAddr2)
                .append(shipaddrAddr3)
                .append(shipaddrAddr4)
                .append(shipaddrPhone)
                .append(shipaddrFax)
                .append(shipaddrEmail)
                .append(terms)
                .append(taxIncluded)
                .append(taxtable)
                .toHashCode();
    }
}

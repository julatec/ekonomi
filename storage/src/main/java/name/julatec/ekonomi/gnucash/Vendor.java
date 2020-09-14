package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "vendors")
public class Vendor {
    private String guid;
    private String name;
    private String id;
    private String notes;
    private String currency;
    private int active;
    private int taxOverride;
    private String addrName;
    private String addrAddr1;
    private String addrAddr2;
    private String addrAddr3;
    private String addrAddr4;
    private String addrPhone;
    private String addrFax;
    private String addrEmail;
    private String terms;
    private String taxInc;
    private String taxTable;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Vendor setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 2048)
    public String getName() {
        return name;
    }

    public Vendor setName(String name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "id", nullable = false, length = 2048)
    public String getId() {
        return id;
    }

    public Vendor setId(String id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "notes", nullable = false, length = 2048)
    public String getNotes() {
        return notes;
    }

    public Vendor setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    @Basic
    @Column(name = "currency", nullable = false, length = 32)
    public String getCurrency() {
        return currency;
    }

    public Vendor setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    @Basic
    @Column(name = "active", nullable = false)
    public int getActive() {
        return active;
    }

    public Vendor setActive(int active) {
        this.active = active;
        return this;
    }

    @Basic
    @Column(name = "tax_override", nullable = false)
    public int getTaxOverride() {
        return taxOverride;
    }

    public Vendor setTaxOverride(int taxOverride) {
        this.taxOverride = taxOverride;
        return this;
    }

    @Basic
    @Column(name = "addr_name", nullable = true, length = 1024)
    public String getAddrName() {
        return addrName;
    }

    public Vendor setAddrName(String addrName) {
        this.addrName = addrName;
        return this;
    }

    @Basic
    @Column(name = "addr_addr1", nullable = true, length = 1024)
    public String getAddrAddr1() {
        return addrAddr1;
    }

    public Vendor setAddrAddr1(String addrAddr1) {
        this.addrAddr1 = addrAddr1;
        return this;
    }

    @Basic
    @Column(name = "addr_addr2", nullable = true, length = 1024)
    public String getAddrAddr2() {
        return addrAddr2;
    }

    public Vendor setAddrAddr2(String addrAddr2) {
        this.addrAddr2 = addrAddr2;
        return this;
    }

    @Basic
    @Column(name = "addr_addr3", nullable = true, length = 1024)
    public String getAddrAddr3() {
        return addrAddr3;
    }

    public Vendor setAddrAddr3(String addrAddr3) {
        this.addrAddr3 = addrAddr3;
        return this;
    }

    @Basic
    @Column(name = "addr_addr4", nullable = true, length = 1024)
    public String getAddrAddr4() {
        return addrAddr4;
    }

    public Vendor setAddrAddr4(String addrAddr4) {
        this.addrAddr4 = addrAddr4;
        return this;
    }

    @Basic
    @Column(name = "addr_phone", nullable = true, length = 128)
    public String getAddrPhone() {
        return addrPhone;
    }

    public Vendor setAddrPhone(String addrPhone) {
        this.addrPhone = addrPhone;
        return this;
    }

    @Basic
    @Column(name = "addr_fax", nullable = true, length = 128)
    public String getAddrFax() {
        return addrFax;
    }

    public Vendor setAddrFax(String addrFax) {
        this.addrFax = addrFax;
        return this;
    }

    @Basic
    @Column(name = "addr_email", nullable = true, length = 256)
    public String getAddrEmail() {
        return addrEmail;
    }

    public Vendor setAddrEmail(String addrEmail) {
        this.addrEmail = addrEmail;
        return this;
    }

    @Basic
    @Column(name = "terms", nullable = true, length = 32)
    public String getTerms() {
        return terms;
    }

    public Vendor setTerms(String terms) {
        this.terms = terms;
        return this;
    }

    @Basic
    @Column(name = "tax_inc", nullable = true, length = 2048)
    public String getTaxInc() {
        return taxInc;
    }

    public Vendor setTaxInc(String taxInc) {
        this.taxInc = taxInc;
        return this;
    }

    @Basic
    @Column(name = "tax_table", nullable = true, length = 32)
    public String getTaxTable() {
        return taxTable;
    }

    public Vendor setTaxTable(String taxTable) {
        this.taxTable = taxTable;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Vendor vendor = (Vendor) o;

        return new EqualsBuilder()
                .append(active, vendor.active)
                .append(taxOverride, vendor.taxOverride)
                .append(guid, vendor.guid)
                .append(name, vendor.name)
                .append(id, vendor.id)
                .append(notes, vendor.notes)
                .append(currency, vendor.currency)
                .append(addrName, vendor.addrName)
                .append(addrAddr1, vendor.addrAddr1)
                .append(addrAddr2, vendor.addrAddr2)
                .append(addrAddr3, vendor.addrAddr3)
                .append(addrAddr4, vendor.addrAddr4)
                .append(addrPhone, vendor.addrPhone)
                .append(addrFax, vendor.addrFax)
                .append(addrEmail, vendor.addrEmail)
                .append(terms, vendor.terms)
                .append(taxInc, vendor.taxInc)
                .append(taxTable, vendor.taxTable)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(name)
                .append(id)
                .append(notes)
                .append(currency)
                .append(active)
                .append(taxOverride)
                .append(addrName)
                .append(addrAddr1)
                .append(addrAddr2)
                .append(addrAddr3)
                .append(addrAddr4)
                .append(addrPhone)
                .append(addrFax)
                .append(addrEmail)
                .append(terms)
                .append(taxInc)
                .append(taxTable)
                .toHashCode();
    }
}

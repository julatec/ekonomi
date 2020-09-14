package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(name = "employees")
public class Employee {
    private String guid;
    private String username;
    private String id;
    private String language;
    private String acl;
    private int active;
    private String currency;
    private String ccardGuid;
    private long workdayNum;
    private long workdayDenom;
    private long rateNum;
    private long rateDenom;
    private String addrName;
    private String addrAddr1;
    private String addrAddr2;
    private String addrAddr3;
    private String addrAddr4;
    private String addrPhone;
    private String addrFax;
    private String addrEmail;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Employee setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "username", nullable = false, length = 2048)
    public String getUsername() {
        return username;
    }

    public Employee setUsername(String username) {
        this.username = username;
        return this;
    }

    @Basic
    @Column(name = "id", nullable = false, length = 2048)
    public String getId() {
        return id;
    }

    public Employee setId(String id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "language", nullable = false, length = 2048)
    public String getLanguage() {
        return language;
    }

    public Employee setLanguage(String language) {
        this.language = language;
        return this;
    }

    @Basic
    @Column(name = "acl", nullable = false, length = 2048)
    public String getAcl() {
        return acl;
    }

    public Employee setAcl(String acl) {
        this.acl = acl;
        return this;
    }

    @Basic
    @Column(name = "active", nullable = false)
    public int getActive() {
        return active;
    }

    public Employee setActive(int active) {
        this.active = active;
        return this;
    }

    @Basic
    @Column(name = "currency", nullable = false, length = 32)
    public String getCurrency() {
        return currency;
    }

    public Employee setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    @Basic
    @Column(name = "ccard_guid", nullable = true, length = 32)
    public String getCcardGuid() {
        return ccardGuid;
    }

    public Employee setCcardGuid(String ccardGuid) {
        this.ccardGuid = ccardGuid;
        return this;
    }

    @Basic
    @Column(name = "workday_num", nullable = false)
    public long getWorkdayNum() {
        return workdayNum;
    }

    public Employee setWorkdayNum(long workdayNum) {
        this.workdayNum = workdayNum;
        return this;
    }

    @Basic
    @Column(name = "workday_denom", nullable = false)
    public long getWorkdayDenom() {
        return workdayDenom;
    }

    public Employee setWorkdayDenom(long workdayDenom) {
        this.workdayDenom = workdayDenom;
        return this;
    }

    @Basic
    @Column(name = "rate_num", nullable = false)
    public long getRateNum() {
        return rateNum;
    }

    public Employee setRateNum(long rateNum) {
        this.rateNum = rateNum;
        return this;
    }

    @Basic
    @Column(name = "rate_denom", nullable = false)
    public long getRateDenom() {
        return rateDenom;
    }

    public Employee setRateDenom(long rateDenom) {
        this.rateDenom = rateDenom;
        return this;
    }

    @Basic
    @Column(name = "addr_name", nullable = true, length = 1024)
    public String getAddrName() {
        return addrName;
    }

    public Employee setAddrName(String addrName) {
        this.addrName = addrName;
        return this;
    }

    @Basic
    @Column(name = "addr_addr1", nullable = true, length = 1024)
    public String getAddrAddr1() {
        return addrAddr1;
    }

    public Employee setAddrAddr1(String addrAddr1) {
        this.addrAddr1 = addrAddr1;
        return this;
    }

    @Basic
    @Column(name = "addr_addr2", nullable = true, length = 1024)
    public String getAddrAddr2() {
        return addrAddr2;
    }

    public Employee setAddrAddr2(String addrAddr2) {
        this.addrAddr2 = addrAddr2;
        return this;
    }

    @Basic
    @Column(name = "addr_addr3", nullable = true, length = 1024)
    public String getAddrAddr3() {
        return addrAddr3;
    }

    public Employee setAddrAddr3(String addrAddr3) {
        this.addrAddr3 = addrAddr3;
        return this;
    }

    @Basic
    @Column(name = "addr_addr4", nullable = true, length = 1024)
    public String getAddrAddr4() {
        return addrAddr4;
    }

    public Employee setAddrAddr4(String addrAddr4) {
        this.addrAddr4 = addrAddr4;
        return this;
    }

    @Basic
    @Column(name = "addr_phone", nullable = true, length = 128)
    public String getAddrPhone() {
        return addrPhone;
    }

    public Employee setAddrPhone(String addrPhone) {
        this.addrPhone = addrPhone;
        return this;
    }

    @Basic
    @Column(name = "addr_fax", nullable = true, length = 128)
    public String getAddrFax() {
        return addrFax;
    }

    public Employee setAddrFax(String addrFax) {
        this.addrFax = addrFax;
        return this;
    }

    @Basic
    @Column(name = "addr_email", nullable = true, length = 256)
    public String getAddrEmail() {
        return addrEmail;
    }

    public Employee setAddrEmail(String addrEmail) {
        this.addrEmail = addrEmail;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Employee employee = (Employee) o;

        return new EqualsBuilder()
                .append(active, employee.active)
                .append(workdayNum, employee.workdayNum)
                .append(workdayDenom, employee.workdayDenom)
                .append(rateNum, employee.rateNum)
                .append(rateDenom, employee.rateDenom)
                .append(guid, employee.guid)
                .append(username, employee.username)
                .append(id, employee.id)
                .append(language, employee.language)
                .append(acl, employee.acl)
                .append(currency, employee.currency)
                .append(ccardGuid, employee.ccardGuid)
                .append(addrName, employee.addrName)
                .append(addrAddr1, employee.addrAddr1)
                .append(addrAddr2, employee.addrAddr2)
                .append(addrAddr3, employee.addrAddr3)
                .append(addrAddr4, employee.addrAddr4)
                .append(addrPhone, employee.addrPhone)
                .append(addrFax, employee.addrFax)
                .append(addrEmail, employee.addrEmail)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(username)
                .append(id)
                .append(language)
                .append(acl)
                .append(active)
                .append(currency)
                .append(ccardGuid)
                .append(workdayNum)
                .append(workdayDenom)
                .append(rateNum)
                .append(rateDenom)
                .append(addrName)
                .append(addrAddr1)
                .append(addrAddr2)
                .append(addrAddr3)
                .append(addrAddr4)
                .append(addrPhone)
                .append(addrFax)
                .append(addrEmail)
                .toHashCode();
    }
}

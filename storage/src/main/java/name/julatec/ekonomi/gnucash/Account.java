package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.*;

import javax.persistence.*;
import java.io.Serializable;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "accounts",
        indexes = {
                @Index(name = "code_index", columnList = "code", unique = true),
        })
public class Account implements Serializable, Comparable<Account> {

    private String guid;
    private String name;
    private String accountType;
    private String commodityGuid;
    private int commodityScu;
    private int nonStdScu;
    private String parentGuid;
    private String code;
    private String description;
    private Integer hidden;
    private Integer placeholder;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Account setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 2048)
    public String getName() {
        return name;
    }

    public Account setName(String name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "account_type", nullable = false, length = 2048)
    public String getAccountType() {
        return accountType;
    }

    public Account setAccountType(String accountType) {
        this.accountType = accountType;
        return this;
    }

    @Basic
    @Column(name = "commodity_guid", nullable = true, length = 32)
    public String getCommodityGuid() {
        return commodityGuid;
    }

    public Account setCommodityGuid(String commodityGuid) {
        this.commodityGuid = commodityGuid;
        return this;
    }

    @Basic
    @Column(name = "commodity_scu", nullable = false)
    public int getCommodityScu() {
        return commodityScu;
    }

    public Account setCommodityScu(int commodityScu) {
        this.commodityScu = commodityScu;
        return this;
    }

    @Basic
    @Column(name = "non_std_scu", nullable = false)
    public int getNonStdScu() {
        return nonStdScu;
    }

    public Account setNonStdScu(int nonStdScu) {
        this.nonStdScu = nonStdScu;
        return this;
    }

    @Basic
    @Column(name = "parent_guid", nullable = true, length = 32)
    public String getParentGuid() {
        return parentGuid;
    }

    public Account setParentGuid(String parentGuid) {
        this.parentGuid = parentGuid;
        return this;
    }

    @Basic
    @Column(name = "code", nullable = true, length = 32, unique = true)
    public String getCode() {
        return code;
    }

    public Account setCode(String code) {
        this.code = code;
        return this;
    }

    @Basic
    @Column(name = "description", nullable = true, length = 2048)
    public String getDescription() {
        return description;
    }

    public Account setDescription(String description) {
        this.description = description;
        return this;
    }

    @Basic
    @Column(name = "hidden", nullable = true)
    public Integer getHidden() {
        return hidden;
    }

    public Account setHidden(Integer hidden) {
        this.hidden = hidden;
        return this;
    }

    @Basic
    @Column(name = "placeholder", nullable = true)
    public Integer getPlaceholder() {
        return placeholder;
    }

    public Account setPlaceholder(Integer placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return new EqualsBuilder()
                .append(commodityScu, account.commodityScu)
                .append(nonStdScu, account.nonStdScu)
                .append(guid, account.guid)
                .append(name, account.name)
                .append(accountType, account.accountType)
                .append(commodityGuid, account.commodityGuid)
                .append(parentGuid, account.parentGuid)
                .append(code, account.code)
                .append(description, account.description)
                .append(hidden, account.hidden)
                .append(placeholder, account.placeholder)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(name)
                .append(accountType)
                .append(commodityGuid)
                .append(commodityScu)
                .append(nonStdScu)
                .append(parentGuid)
                .append(code)
                .append(description)
                .append(hidden)
                .append(placeholder)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("guid", guid)
                .append("name", name)
                .append("code", code)
                .toString();
    }

    @Override
    public int compareTo(Account that) {
        return new CompareToBuilder().append(this.code, that.code).toComparison();
    }
}

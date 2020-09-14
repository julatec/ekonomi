package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "billterms")
public class Billterm {
    private String guid;
    private String name;
    private String description;
    private int refcount;
    private int invisible;
    private String parent;
    private String type;
    private Integer duedays;
    private Integer discountdays;
    private Long discountNum;
    private Long discountDenom;
    private Integer cutoff;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Billterm setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 2048)
    public String getName() {
        return name;
    }

    public Billterm setName(String name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "description", nullable = false, length = 2048)
    public String getDescription() {
        return description;
    }

    public Billterm setDescription(String description) {
        this.description = description;
        return this;
    }

    @Basic
    @Column(name = "refcount", nullable = false)
    public int getRefcount() {
        return refcount;
    }

    public Billterm setRefcount(int refcount) {
        this.refcount = refcount;
        return this;
    }

    @Basic
    @Column(name = "invisible", nullable = false)
    public int getInvisible() {
        return invisible;
    }

    public Billterm setInvisible(int invisible) {
        this.invisible = invisible;
        return this;
    }

    @Basic
    @Column(name = "parent", nullable = true, length = 32)
    public String getParent() {
        return parent;
    }

    public Billterm setParent(String parent) {
        this.parent = parent;
        return this;
    }

    @Basic
    @Column(name = "type", nullable = false, length = 2048)
    public String getType() {
        return type;
    }

    public Billterm setType(String type) {
        this.type = type;
        return this;
    }

    @Basic
    @Column(name = "duedays", nullable = true)
    public Integer getDuedays() {
        return duedays;
    }

    public Billterm setDuedays(Integer duedays) {
        this.duedays = duedays;
        return this;
    }

    @Basic
    @Column(name = "discountdays", nullable = true)
    public Integer getDiscountdays() {
        return discountdays;
    }

    public Billterm setDiscountdays(Integer discountdays) {
        this.discountdays = discountdays;
        return this;
    }

    @Basic
    @Column(name = "discount_num", nullable = true)
    public Long getDiscountNum() {
        return discountNum;
    }

    public Billterm setDiscountNum(Long discountNum) {
        this.discountNum = discountNum;
        return this;
    }

    @Basic
    @Column(name = "discount_denom", nullable = true)
    public Long getDiscountDenom() {
        return discountDenom;
    }

    public Billterm setDiscountDenom(Long discountDenom) {
        this.discountDenom = discountDenom;
        return this;
    }

    @Basic
    @Column(name = "cutoff", nullable = true)
    public Integer getCutoff() {
        return cutoff;
    }

    public Billterm setCutoff(Integer cutoff) {
        this.cutoff = cutoff;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Billterm billterm = (Billterm) o;

        return new EqualsBuilder()
                .append(refcount, billterm.refcount)
                .append(invisible, billterm.invisible)
                .append(guid, billterm.guid)
                .append(name, billterm.name)
                .append(description, billterm.description)
                .append(parent, billterm.parent)
                .append(type, billterm.type)
                .append(duedays, billterm.duedays)
                .append(discountdays, billterm.discountdays)
                .append(discountNum, billterm.discountNum)
                .append(discountDenom, billterm.discountDenom)
                .append(cutoff, billterm.cutoff)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(name)
                .append(description)
                .append(refcount)
                .append(invisible)
                .append(parent)
                .append(type)
                .append(duedays)
                .append(discountdays)
                .append(discountNum)
                .append(discountDenom)
                .append(cutoff)
                .toHashCode();
    }
}

package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")

@Entity
@Table(name = "taxtables")
public class Taxtable {
    private String guid;
    private String name;
    private long refcount;
    private int invisible;
    private String parent;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Taxtable setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 50)
    public String getName() {
        return name;
    }

    public Taxtable setName(String name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "refcount", nullable = false)
    public long getRefcount() {
        return refcount;
    }

    public Taxtable setRefcount(long refcount) {
        this.refcount = refcount;
        return this;
    }

    @Basic
    @Column(name = "invisible", nullable = false)
    public int getInvisible() {
        return invisible;
    }

    public Taxtable setInvisible(int invisible) {
        this.invisible = invisible;
        return this;
    }

    @Basic
    @Column(name = "parent", nullable = true, length = 32)
    public String getParent() {
        return parent;
    }

    public Taxtable setParent(String parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Taxtable taxtable = (Taxtable) o;

        return new EqualsBuilder()
                .append(refcount, taxtable.refcount)
                .append(invisible, taxtable.invisible)
                .append(guid, taxtable.guid)
                .append(name, taxtable.name)
                .append(parent, taxtable.parent)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(name)
                .append(refcount)
                .append(invisible)
                .append(parent)
                .toHashCode();
    }
}

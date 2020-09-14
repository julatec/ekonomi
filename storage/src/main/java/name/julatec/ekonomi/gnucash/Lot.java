package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "lots")
public class Lot {
    private String guid;
    private String accountGuid;
    private int isClosed;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Lot setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "account_guid", nullable = true, length = 32)
    public String getAccountGuid() {
        return accountGuid;
    }

    public Lot setAccountGuid(String accountGuid) {
        this.accountGuid = accountGuid;
        return this;
    }

    @Basic
    @Column(name = "is_closed", nullable = false)
    public int getIsClosed() {
        return isClosed;
    }

    public Lot setIsClosed(int isClosed) {
        this.isClosed = isClosed;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Lot lot = (Lot) o;

        return new EqualsBuilder()
                .append(isClosed, lot.isClosed)
                .append(guid, lot.guid)
                .append(accountGuid, lot.accountGuid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(accountGuid)
                .append(isClosed)
                .toHashCode();
    }
}

package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "taxtable_entries")
public class TaxtableEntry {
    private int id;
    private String taxtable;
    private String account;
    private long amountNum;
    private long amountDenom;
    private int type;

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public TaxtableEntry setId(int id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "taxtable", nullable = false, length = 32)
    public String getTaxtable() {
        return taxtable;
    }

    public TaxtableEntry setTaxtable(String taxtable) {
        this.taxtable = taxtable;
        return this;
    }

    @Basic
    @Column(name = "account", nullable = false, length = 32)
    public String getAccount() {
        return account;
    }

    public TaxtableEntry setAccount(String account) {
        this.account = account;
        return this;
    }

    @Basic
    @Column(name = "amount_num", nullable = false)
    public long getAmountNum() {
        return amountNum;
    }

    public TaxtableEntry setAmountNum(long amountNum) {
        this.amountNum = amountNum;
        return this;
    }

    @Basic
    @Column(name = "amount_denom", nullable = false)
    public long getAmountDenom() {
        return amountDenom;
    }

    public TaxtableEntry setAmountDenom(long amountDenom) {
        this.amountDenom = amountDenom;
        return this;
    }

    @Basic
    @Column(name = "type", nullable = false)
    public int getType() {
        return type;
    }

    public TaxtableEntry setType(int type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TaxtableEntry that = (TaxtableEntry) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(amountNum, that.amountNum)
                .append(amountDenom, that.amountDenom)
                .append(type, that.type)
                .append(taxtable, that.taxtable)
                .append(account, that.account)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(taxtable)
                .append(account)
                .append(amountNum)
                .append(amountDenom)
                .append(type)
                .toHashCode();
    }
}

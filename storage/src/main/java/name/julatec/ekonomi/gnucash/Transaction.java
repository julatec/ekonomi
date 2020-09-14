package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "transactions")
public class Transaction {
    private String guid;
    private String currencyGuid;
    private String num;
    private Timestamp postDate;
    private Timestamp enterDate;
    private String description;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Transaction setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "currency_guid", nullable = false, length = 32)
    public String getCurrencyGuid() {
        return currencyGuid;
    }

    public Transaction setCurrencyGuid(String currencyGuid) {
        this.currencyGuid = currencyGuid;
        return this;
    }

    @Basic
    @Column(name = "num", nullable = false, length = 2048)
    public String getNum() {
        return num;
    }

    public Transaction setNum(String num) {
        this.num = num;
        return this;
    }

    @Basic
    @Column(name = "post_date", nullable = true)
    public Timestamp getPostDate() {
        return postDate;
    }

    public Transaction setPostDate(Timestamp postDate) {
        this.postDate = postDate;
        return this;
    }

    @Basic
    @Column(name = "enter_date", nullable = true)
    public Timestamp getEnterDate() {
        return enterDate;
    }

    public Transaction setEnterDate(Timestamp enterDate) {
        this.enterDate = enterDate;
        return this;
    }

    @Basic
    @Column(name = "description", nullable = true, length = 2048)
    public String getDescription() {
        return description;
    }

    public Transaction setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        return new EqualsBuilder()
                .append(guid, that.guid)
                .append(currencyGuid, that.currencyGuid)
                .append(num, that.num)
                .append(postDate, that.postDate)
                .append(enterDate, that.enterDate)
                .append(description, that.description)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(currencyGuid)
                .append(num)
                .append(postDate)
                .append(enterDate)
                .append(description)
                .toHashCode();
    }
}

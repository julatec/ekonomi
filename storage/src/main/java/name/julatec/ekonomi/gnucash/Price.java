package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "prices")
public class Price {
    private String guid;
    private String commodityGuid;
    private String currencyGuid;
    private Timestamp date;
    private String source;
    private String type;
    private long valueNum;
    private long valueDenom;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Price setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "commodity_guid", nullable = false, length = 32)
    public String getCommodityGuid() {
        return commodityGuid;
    }

    public Price setCommodityGuid(String commodityGuid) {
        this.commodityGuid = commodityGuid;
        return this;
    }

    @Basic
    @Column(name = "currency_guid", nullable = false, length = 32)
    public String getCurrencyGuid() {
        return currencyGuid;
    }

    public Price setCurrencyGuid(String currencyGuid) {
        this.currencyGuid = currencyGuid;
        return this;
    }

    @Basic
    @Column(name = "date", nullable = false)
    public Timestamp getDate() {
        return date;
    }

    public Price setDate(Timestamp date) {
        this.date = date;
        return this;
    }

    @Basic
    @Column(name = "source", nullable = true, length = 2048)
    public String getSource() {
        return source;
    }

    public Price setSource(String source) {
        this.source = source;
        return this;
    }

    @Basic
    @Column(name = "type", nullable = true, length = 2048)
    public String getType() {
        return type;
    }

    public Price setType(String type) {
        this.type = type;
        return this;
    }

    @Basic
    @Column(name = "value_num", nullable = false)
    public long getValueNum() {
        return valueNum;
    }

    public Price setValueNum(long valueNum) {
        this.valueNum = valueNum;
        return this;
    }

    @Basic
    @Column(name = "value_denom", nullable = false)
    public long getValueDenom() {
        return valueDenom;
    }

    public Price setValueDenom(long valueDenom) {
        this.valueDenom = valueDenom;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Price price = (Price) o;

        return new EqualsBuilder()
                .append(valueNum, price.valueNum)
                .append(valueDenom, price.valueDenom)
                .append(guid, price.guid)
                .append(commodityGuid, price.commodityGuid)
                .append(currencyGuid, price.currencyGuid)
                .append(date, price.date)
                .append(source, price.source)
                .append(type, price.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(commodityGuid)
                .append(currencyGuid)
                .append(date)
                .append(source)
                .append(type)
                .append(valueNum)
                .append(valueDenom)
                .toHashCode();
    }
}

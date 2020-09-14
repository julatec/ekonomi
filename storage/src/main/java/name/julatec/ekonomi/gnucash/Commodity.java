package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "commodities")
public class Commodity {
    private String guid;
    private String namespace;
    private String mnemonic;
    private String fullname;
    private String cusip;
    private int fraction;
    private int quoteFlag;
    private String quoteSource;
    private String quoteTz;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Commodity setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "namespace", nullable = false, length = 2048)
    public String getNamespace() {
        return namespace;
    }

    public Commodity setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Basic
    @Column(name = "mnemonic", nullable = false, length = 2048)
    public String getMnemonic() {
        return mnemonic;
    }

    public Commodity setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
        return this;
    }

    @Basic
    @Column(name = "fullname", nullable = true, length = 2048)
    public String getFullname() {
        return fullname;
    }

    public Commodity setFullname(String fullname) {
        this.fullname = fullname;
        return this;
    }

    @Basic
    @Column(name = "cusip", nullable = true, length = 2048)
    public String getCusip() {
        return cusip;
    }

    public Commodity setCusip(String cusip) {
        this.cusip = cusip;
        return this;
    }

    @Basic
    @Column(name = "fraction", nullable = false)
    public int getFraction() {
        return fraction;
    }

    public Commodity setFraction(int fraction) {
        this.fraction = fraction;
        return this;
    }

    @Basic
    @Column(name = "quote_flag", nullable = false)
    public int getQuoteFlag() {
        return quoteFlag;
    }

    public Commodity setQuoteFlag(int quoteFlag) {
        this.quoteFlag = quoteFlag;
        return this;
    }

    @Basic
    @Column(name = "quote_source", nullable = true, length = 2048)
    public String getQuoteSource() {
        return quoteSource;
    }

    public Commodity setQuoteSource(String quoteSource) {
        this.quoteSource = quoteSource;
        return this;
    }

    @Basic
    @Column(name = "quote_tz", nullable = true, length = 2048)
    public String getQuoteTz() {
        return quoteTz;
    }

    public Commodity setQuoteTz(String quoteTz) {
        this.quoteTz = quoteTz;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Commodity commodity = (Commodity) o;

        return new EqualsBuilder()
                .append(fraction, commodity.fraction)
                .append(quoteFlag, commodity.quoteFlag)
                .append(guid, commodity.guid)
                .append(namespace, commodity.namespace)
                .append(mnemonic, commodity.mnemonic)
                .append(fullname, commodity.fullname)
                .append(cusip, commodity.cusip)
                .append(quoteSource, commodity.quoteSource)
                .append(quoteTz, commodity.quoteTz)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(namespace)
                .append(mnemonic)
                .append(fullname)
                .append(cusip)
                .append(fraction)
                .append(quoteFlag)
                .append(quoteSource)
                .append(quoteTz)
                .toHashCode();
    }
}

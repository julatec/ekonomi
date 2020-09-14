package name.julatec.ekonomi.security;

import org.apache.commons.lang3.builder.CompareToBuilder;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Currency;
import java.util.UUID;

import static name.julatec.ekonomi.tribunet.UuidFormatter.uuidFromString;
import static name.julatec.ekonomi.tribunet.UuidFormatter.uuidToString;

@MappedSuperclass
public abstract class ImportTransaction<A extends ImportTransaction<A>> implements Comparable<A> {

    protected String remoteId;
    protected String name;
    protected String defaultCurrency = "CRC";
    protected String persistenceUnit;
    protected String ownerId;
    protected String ownerName;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    protected String guid;

    public String getOwnerId() {
        return ownerId;
    }

    public A setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return (A) this;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public A setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        return (A) this;
    }

    public UUID getAccount() {
        return uuidFromString(guid);
    }

    public A setAccount(UUID account) {
        this.guid = uuidToString(account);
        return (A) this;
    }

    public Currency getDefaultCurrency() {
        return Currency.getInstance(defaultCurrency);
    }

    public void setDefaultCurrency(Currency currency) {
        this.defaultCurrency = currency.getCurrencyCode();
    }

    public A setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
        return (A) this;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public A setRemoteId(String remoteId) {
        this.remoteId = remoteId;
        return (A) this;
    }

    public String getName() {
        return name;
    }

    public A setName(String name) {
        this.name = name;
        return (A) this;
    }

    public String getPersistenceUnit() {
        return persistenceUnit;
    }

    public A setPersistenceUnit(String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
        return (A) this;
    }

    @Override
    public int compareTo(A that) {
        return new CompareToBuilder().append(this.getName(), that.getName()).toComparison();
    }
}

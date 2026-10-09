package name.julatec.ekonomi.security;

import org.apache.commons.lang3.builder.CompareToBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
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

    // `A extends ImportTransaction<A>` obliga a que A sea la subclase concreta, así que `this`
    // siempre es un A. `javac` no puede demostrarlo —es el idioma de «tipo propio» y no tiene
    // forma verificable en Java—, por eso el cast se declara aquí una sola vez y no en cada setter.
    @SuppressWarnings("unchecked")
    private A self() {
        return (A) this;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public A setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return self();
    }

    public String getOwnerName() {
        return ownerName;
    }

    public A setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        return self();
    }

    public UUID getAccount() {
        return uuidFromString(guid);
    }

    public A setAccount(UUID account) {
        this.guid = uuidToString(account);
        return self();
    }

    public Currency getDefaultCurrency() {
        return Currency.getInstance(defaultCurrency);
    }

    public void setDefaultCurrency(Currency currency) {
        this.defaultCurrency = currency.getCurrencyCode();
    }

    public A setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
        return self();
    }

    public String getRemoteId() {
        return remoteId;
    }

    public A setRemoteId(String remoteId) {
        this.remoteId = remoteId;
        return self();
    }

    public String getName() {
        return name;
    }

    public A setName(String name) {
        this.name = name;
        return self();
    }

    public String getPersistenceUnit() {
        return persistenceUnit;
    }

    public A setPersistenceUnit(String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
        return self();
    }

    @Override
    public int compareTo(A that) {
        return new CompareToBuilder().append(this.getName(), that.getName()).toComparison();
    }
}

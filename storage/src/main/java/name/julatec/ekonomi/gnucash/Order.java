package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "orders")
public class Order {
    private String guid;
    private String id;
    private String notes;
    private String reference;
    private int active;
    private Timestamp dateOpened;
    private Timestamp dateClosed;
    private int ownerType;
    private String ownerGuid;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Order setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "id", nullable = false, length = 2048)
    public String getId() {
        return id;
    }

    public Order setId(String id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "notes", nullable = false, length = 2048)
    public String getNotes() {
        return notes;
    }

    public Order setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    @Basic
    @Column(name = "reference", nullable = false, length = 2048)
    public String getReference() {
        return reference;
    }

    public Order setReference(String reference) {
        this.reference = reference;
        return this;
    }

    @Basic
    @Column(name = "active", nullable = false)
    public int getActive() {
        return active;
    }

    public Order setActive(int active) {
        this.active = active;
        return this;
    }

    @Basic
    @Column(name = "date_opened", nullable = false)
    public Timestamp getDateOpened() {
        return dateOpened;
    }

    public Order setDateOpened(Timestamp dateOpened) {
        this.dateOpened = dateOpened;
        return this;
    }

    @Basic
    @Column(name = "date_closed", nullable = false)
    public Timestamp getDateClosed() {
        return dateClosed;
    }

    public Order setDateClosed(Timestamp dateClosed) {
        this.dateClosed = dateClosed;
        return this;
    }

    @Basic
    @Column(name = "owner_type", nullable = false)
    public int getOwnerType() {
        return ownerType;
    }

    public Order setOwnerType(int ownerType) {
        this.ownerType = ownerType;
        return this;
    }

    @Basic
    @Column(name = "owner_guid", nullable = false, length = 32)
    public String getOwnerGuid() {
        return ownerGuid;
    }

    public Order setOwnerGuid(String ownerGuid) {
        this.ownerGuid = ownerGuid;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        return new EqualsBuilder()
                .append(active, order.active)
                .append(ownerType, order.ownerType)
                .append(guid, order.guid)
                .append(id, order.id)
                .append(notes, order.notes)
                .append(reference, order.reference)
                .append(dateOpened, order.dateOpened)
                .append(dateClosed, order.dateClosed)
                .append(ownerGuid, order.ownerGuid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(id)
                .append(notes)
                .append(reference)
                .append(active)
                .append(dateOpened)
                .append(dateClosed)
                .append(ownerType)
                .append(ownerGuid)
                .toHashCode();
    }
}

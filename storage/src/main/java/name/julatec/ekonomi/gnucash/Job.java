package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(name = "jobs")
public class Job {
    private String guid;
    private String id;
    private String name;
    private String reference;
    private int active;
    private Integer ownerType;
    private String ownerGuid;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Job setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "id", nullable = false, length = 2048)
    public String getId() {
        return id;
    }

    public Job setId(String id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 2048)
    public String getName() {
        return name;
    }

    public Job setName(String name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "reference", nullable = false, length = 2048)
    public String getReference() {
        return reference;
    }

    public Job setReference(String reference) {
        this.reference = reference;
        return this;
    }

    @Basic
    @Column(name = "active", nullable = false)
    public int getActive() {
        return active;
    }

    public Job setActive(int active) {
        this.active = active;
        return this;
    }

    @Basic
    @Column(name = "owner_type", nullable = true)
    public Integer getOwnerType() {
        return ownerType;
    }

    public Job setOwnerType(Integer ownerType) {
        this.ownerType = ownerType;
        return this;
    }

    @Basic
    @Column(name = "owner_guid", nullable = true, length = 32)
    public String getOwnerGuid() {
        return ownerGuid;
    }

    public Job setOwnerGuid(String ownerGuid) {
        this.ownerGuid = ownerGuid;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        return new EqualsBuilder()
                .append(active, job.active)
                .append(guid, job.guid)
                .append(id, job.id)
                .append(name, job.name)
                .append(reference, job.reference)
                .append(ownerType, job.ownerType)
                .append(ownerGuid, job.ownerGuid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(id)
                .append(name)
                .append(reference)
                .append(active)
                .append(ownerType)
                .append(ownerGuid)
                .toHashCode();
    }
}

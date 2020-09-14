package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Date;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "schedxactions")
public class SchedxAction {
    private String guid;
    private String name;
    private int enabled;
    private Date startDate;
    private Date endDate;
    private Date lastOccur;
    private int numOccur;
    private int remOccur;
    private int autoCreate;
    private int autoNotify;
    private int advCreation;
    private int advNotify;
    private int instanceCount;
    private String templateActGuid;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public SchedxAction setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "name", nullable = true, length = 2048)
    public String getName() {
        return name;
    }

    public SchedxAction setName(String name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "enabled", nullable = false)
    public int getEnabled() {
        return enabled;
    }

    public SchedxAction setEnabled(int enabled) {
        this.enabled = enabled;
        return this;
    }

    @Basic
    @Column(name = "start_date", nullable = true)
    public Date getStartDate() {
        return startDate;
    }

    public SchedxAction setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    @Basic
    @Column(name = "end_date", nullable = true)
    public Date getEndDate() {
        return endDate;
    }

    public SchedxAction setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    @Basic
    @Column(name = "last_occur", nullable = true)
    public Date getLastOccur() {
        return lastOccur;
    }

    public SchedxAction setLastOccur(Date lastOccur) {
        this.lastOccur = lastOccur;
        return this;
    }

    @Basic
    @Column(name = "num_occur", nullable = false)
    public int getNumOccur() {
        return numOccur;
    }

    public SchedxAction setNumOccur(int numOccur) {
        this.numOccur = numOccur;
        return this;
    }

    @Basic
    @Column(name = "rem_occur", nullable = false)
    public int getRemOccur() {
        return remOccur;
    }

    public SchedxAction setRemOccur(int remOccur) {
        this.remOccur = remOccur;
        return this;
    }

    @Basic
    @Column(name = "auto_create", nullable = false)
    public int getAutoCreate() {
        return autoCreate;
    }

    public SchedxAction setAutoCreate(int autoCreate) {
        this.autoCreate = autoCreate;
        return this;
    }

    @Basic
    @Column(name = "auto_notify", nullable = false)
    public int getAutoNotify() {
        return autoNotify;
    }

    public SchedxAction setAutoNotify(int autoNotify) {
        this.autoNotify = autoNotify;
        return this;
    }

    @Basic
    @Column(name = "adv_creation", nullable = false)
    public int getAdvCreation() {
        return advCreation;
    }

    public SchedxAction setAdvCreation(int advCreation) {
        this.advCreation = advCreation;
        return this;
    }

    @Basic
    @Column(name = "adv_notify", nullable = false)
    public int getAdvNotify() {
        return advNotify;
    }

    public SchedxAction setAdvNotify(int advNotify) {
        this.advNotify = advNotify;
        return this;
    }

    @Basic
    @Column(name = "instance_count", nullable = false)
    public int getInstanceCount() {
        return instanceCount;
    }

    public SchedxAction setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
        return this;
    }

    @Basic
    @Column(name = "template_act_guid", nullable = false, length = 32)
    public String getTemplateActGuid() {
        return templateActGuid;
    }

    public SchedxAction setTemplateActGuid(String templateActGuid) {
        this.templateActGuid = templateActGuid;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SchedxAction that = (SchedxAction) o;

        return new EqualsBuilder()
                .append(enabled, that.enabled)
                .append(numOccur, that.numOccur)
                .append(remOccur, that.remOccur)
                .append(autoCreate, that.autoCreate)
                .append(autoNotify, that.autoNotify)
                .append(advCreation, that.advCreation)
                .append(advNotify, that.advNotify)
                .append(instanceCount, that.instanceCount)
                .append(guid, that.guid)
                .append(name, that.name)
                .append(startDate, that.startDate)
                .append(endDate, that.endDate)
                .append(lastOccur, that.lastOccur)
                .append(templateActGuid, that.templateActGuid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(name)
                .append(enabled)
                .append(startDate)
                .append(endDate)
                .append(lastOccur)
                .append(numOccur)
                .append(remOccur)
                .append(autoCreate)
                .append(autoNotify)
                .append(advCreation)
                .append(advNotify)
                .append(instanceCount)
                .append(templateActGuid)
                .toHashCode();
    }
}

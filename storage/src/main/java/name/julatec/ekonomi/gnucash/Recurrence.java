package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Date;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "recurrences")
public class Recurrence {
    private int id;
    private String objGuid;
    private int recurrenceMult;
    private String recurrencePeriodType;
    private Date recurrencePeriodStart;
    private String recurrenceWeekendAdjust;

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public Recurrence setId(int id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "obj_guid", nullable = false, length = 32)
    public String getObjGuid() {
        return objGuid;
    }

    public Recurrence setObjGuid(String objGuid) {
        this.objGuid = objGuid;
        return this;
    }

    @Basic
    @Column(name = "recurrence_mult", nullable = false)
    public int getRecurrenceMult() {
        return recurrenceMult;
    }

    public Recurrence setRecurrenceMult(int recurrenceMult) {
        this.recurrenceMult = recurrenceMult;
        return this;
    }

    @Basic
    @Column(name = "recurrence_period_type", nullable = false, length = 2048)
    public String getRecurrencePeriodType() {
        return recurrencePeriodType;
    }

    public Recurrence setRecurrencePeriodType(String recurrencePeriodType) {
        this.recurrencePeriodType = recurrencePeriodType;
        return this;
    }

    @Basic
    @Column(name = "recurrence_period_start", nullable = false)
    public Date getRecurrencePeriodStart() {
        return recurrencePeriodStart;
    }

    public Recurrence setRecurrencePeriodStart(Date recurrencePeriodStart) {
        this.recurrencePeriodStart = recurrencePeriodStart;
        return this;
    }

    @Basic
    @Column(name = "recurrence_weekend_adjust", nullable = false, length = 2048)
    public String getRecurrenceWeekendAdjust() {
        return recurrenceWeekendAdjust;
    }

    public Recurrence setRecurrenceWeekendAdjust(String recurrenceWeekendAdjust) {
        this.recurrenceWeekendAdjust = recurrenceWeekendAdjust;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Recurrence that = (Recurrence) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(recurrenceMult, that.recurrenceMult)
                .append(objGuid, that.objGuid)
                .append(recurrencePeriodType, that.recurrencePeriodType)
                .append(recurrencePeriodStart, that.recurrencePeriodStart)
                .append(recurrenceWeekendAdjust, that.recurrenceWeekendAdjust)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(objGuid)
                .append(recurrenceMult)
                .append(recurrencePeriodType)
                .append(recurrencePeriodStart)
                .append(recurrenceWeekendAdjust)
                .toHashCode();
    }
}

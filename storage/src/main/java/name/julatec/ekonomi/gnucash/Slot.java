package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "slots")
public class Slot {
    private int id;
    private String objGuid;
    private String name;
    private int slotType;
    private Long int64Val;
    private String stringVal;
    private Double doubleVal;
    private Timestamp timespecVal;
    private String guidVal;
    private Long numericValNum;
    private Long numericValDenom;
    private Date gdateVal;

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public Slot setId(int id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "obj_guid", nullable = false, length = 32)
    public String getObjGuid() {
        return objGuid;
    }

    public Slot setObjGuid(String objGuid) {
        this.objGuid = objGuid;
        return this;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 4096)
    public String getName() {
        return name;
    }

    public Slot setName(String name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "slot_type", nullable = false)
    public int getSlotType() {
        return slotType;
    }

    public Slot setSlotType(int slotType) {
        this.slotType = slotType;
        return this;
    }

    @Basic
    @Column(name = "int64_val", nullable = true)
    public Long getInt64Val() {
        return int64Val;
    }

    public Slot setInt64Val(Long int64Val) {
        this.int64Val = int64Val;
        return this;
    }

    @Basic
    @Column(name = "string_val", nullable = true, length = 4096)
    public String getStringVal() {
        return stringVal;
    }

    public Slot setStringVal(String stringVal) {
        this.stringVal = stringVal;
        return this;
    }

    @Basic
    @Column(name = "double_val", nullable = true, precision = 0)
    public Double getDoubleVal() {
        return doubleVal;
    }

    public Slot setDoubleVal(Double doubleVal) {
        this.doubleVal = doubleVal;
        return this;
    }

    @Basic
    @Column(name = "timespec_val", nullable = true)
    public Timestamp getTimespecVal() {
        return timespecVal;
    }

    public Slot setTimespecVal(Timestamp timespecVal) {
        this.timespecVal = timespecVal;
        return this;
    }

    @Basic
    @Column(name = "guid_val", nullable = true, length = 32)
    public String getGuidVal() {
        return guidVal;
    }

    public Slot setGuidVal(String guidVal) {
        this.guidVal = guidVal;
        return this;
    }

    @Basic
    @Column(name = "numeric_val_num", nullable = true)
    public Long getNumericValNum() {
        return numericValNum;
    }

    public Slot setNumericValNum(Long numericValNum) {
        this.numericValNum = numericValNum;
        return this;
    }

    @Basic
    @Column(name = "numeric_val_denom", nullable = true)
    public Long getNumericValDenom() {
        return numericValDenom;
    }

    public Slot setNumericValDenom(Long numericValDenom) {
        this.numericValDenom = numericValDenom;
        return this;
    }

    @Basic
    @Column(name = "gdate_val", nullable = true)
    public Date getGdateVal() {
        return gdateVal;
    }

    public Slot setGdateVal(Date gdateVal) {
        this.gdateVal = gdateVal;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Slot slot = (Slot) o;

        return new EqualsBuilder()
                .append(id, slot.id)
                .append(slotType, slot.slotType)
                .append(objGuid, slot.objGuid)
                .append(name, slot.name)
                .append(int64Val, slot.int64Val)
                .append(stringVal, slot.stringVal)
                .append(doubleVal, slot.doubleVal)
                .append(timespecVal, slot.timespecVal)
                .append(guidVal, slot.guidVal)
                .append(numericValNum, slot.numericValNum)
                .append(numericValDenom, slot.numericValDenom)
                .append(gdateVal, slot.gdateVal)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(objGuid)
                .append(name)
                .append(slotType)
                .append(int64Val)
                .append(stringVal)
                .append(doubleVal)
                .append(timespecVal)
                .append(guidVal)
                .append(numericValNum)
                .append(numericValDenom)
                .append(gdateVal)
                .toHashCode();
    }
}

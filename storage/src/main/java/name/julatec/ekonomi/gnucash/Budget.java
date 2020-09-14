package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "budgets")
public class Budget {
    private String guid;
    private String name;
    private String description;
    private int numPeriods;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Budget setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 2048)
    public String getName() {
        return name;
    }

    public Budget setName(String name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "description", nullable = true, length = 2048)
    public String getDescription() {
        return description;
    }

    public Budget setDescription(String description) {
        this.description = description;
        return this;
    }

    @Basic
    @Column(name = "num_periods", nullable = false)
    public int getNumPeriods() {
        return numPeriods;
    }

    public Budget setNumPeriods(int numPeriods) {
        this.numPeriods = numPeriods;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Budget budget = (Budget) o;

        return new EqualsBuilder()
                .append(numPeriods, budget.numPeriods)
                .append(guid, budget.guid)
                .append(name, budget.name)
                .append(description, budget.description)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(name)
                .append(description)
                .append(numPeriods)
                .toHashCode();
    }
}

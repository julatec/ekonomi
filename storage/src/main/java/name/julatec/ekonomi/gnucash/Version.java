package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "versions")
public class Version {
    private String tableName;
    private int tableVersion;

    @Id
    @Column(name = "table_name", nullable = false, length = 50)
    public String getTableName() {
        return tableName;
    }

    public Version setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    @Basic
    @Column(name = "table_version", nullable = false)
    public int getTableVersion() {
        return tableVersion;
    }

    public Version setTableVersion(int tableVersion) {
        this.tableVersion = tableVersion;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        return new EqualsBuilder()
                .append(tableVersion, version.tableVersion)
                .append(tableName, version.tableName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(tableName)
                .append(tableVersion)
                .toHashCode();
    }
}

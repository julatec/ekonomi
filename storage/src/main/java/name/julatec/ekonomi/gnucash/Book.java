package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "books")
public class Book {
    private String guid;
    private String rootAccountGuid;
    private String rootTemplateGuid;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Book setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "root_account_guid", nullable = false, length = 32)
    public String getRootAccountGuid() {
        return rootAccountGuid;
    }

    public Book setRootAccountGuid(String rootAccountGuid) {
        this.rootAccountGuid = rootAccountGuid;
        return this;
    }

    @Basic
    @Column(name = "root_template_guid", nullable = false, length = 32)
    public String getRootTemplateGuid() {
        return rootTemplateGuid;
    }

    public Book setRootTemplateGuid(String rootTemplateGuid) {
        this.rootTemplateGuid = rootTemplateGuid;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        return new EqualsBuilder()
                .append(guid, book.guid)
                .append(rootAccountGuid, book.rootAccountGuid)
                .append(rootTemplateGuid, book.rootTemplateGuid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(rootAccountGuid)
                .append(rootTemplateGuid)
                .toHashCode();
    }
}

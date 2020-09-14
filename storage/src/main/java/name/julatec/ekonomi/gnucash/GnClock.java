package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "gnclock")
public class GnClock {

    @Id
    private String hostname;
    private Integer pid;

    @Basic
    @Column(name = "Hostname", nullable = true, length = 255)
    public String getHostname() {
        return hostname;
    }

    public GnClock setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    @Basic
    @Column(name = "PID", nullable = true)
    public Integer getPid() {
        return pid;
    }

    public GnClock setPid(Integer pid) {
        this.pid = pid;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GnClock gnclock = (GnClock) o;

        return new EqualsBuilder()
                .append(hostname, gnclock.hostname)
                .append(pid, gnclock.pid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(hostname)
                .append(pid)
                .toHashCode();
    }
}

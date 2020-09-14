package name.julatec.ekonomi.security;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class UserId implements Serializable, Comparable<UserId> {

    private String value;
    private String issuer;

    public String getValue() {
        return value;
    }

    public UserId setValue(String username) {
        this.value = username;
        return this;
    }

    public String getIssuer() {
        return issuer;
    }

    public UserId setIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserId userId = (UserId) o;

        return new EqualsBuilder()
                .append(value, userId.value)
                .append(issuer, userId.issuer)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .append(issuer)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("username", value)
                .append("issuer", issuer)
                .toString();
    }


    @Override
    public int compareTo(UserId that) {
        return new CompareToBuilder()
                .append(this.issuer, that.issuer)
                .append(this.value, that.value)
                .toComparison()
                ;
    }
}

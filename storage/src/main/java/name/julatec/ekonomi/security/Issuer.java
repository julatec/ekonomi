package name.julatec.ekonomi.security;

import org.apache.commons.lang3.builder.CompareToBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "issuer")
public class Issuer implements Comparable<Issuer> {

    @Id
    private String name;
    private String field;

    public String getName() {
        return name;
    }

    public Issuer setName(String name) {
        this.name = name;
        return this;
    }

    public String getField() {
        return field;
    }

    public Issuer setField(String field) {
        this.field = field;
        return this;
    }

    @Override
    public int compareTo(Issuer issuer) {
        return new CompareToBuilder().append(this.name, issuer.name).toComparison();
    }
}

package name.julatec.ekonomi.accounting;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

@Entity(name = "periods")
public class AccountingPeriod implements Comparable<AccountingPeriod> {

    @Id
    private String code;
    private String name;
    private String regimen;
    private Date lower;
    private Date upper;

    public String getCode() {
        return code;
    }

    public AccountingPeriod setCode(String code) {
        this.code = code;
        return this;
    }

    public Date getLower() {
        return lower;
    }

    public AccountingPeriod setLower(Date lower) {
        this.lower = lower;
        return this;
    }

    public Date getUpper() {
        return upper;
    }

    public AccountingPeriod setUpper(Date upper) {
        this.upper = upper;
        return this;
    }

    public String getName() {
        return name;
    }

    public AccountingPeriod setName(String name) {
        this.name = name;
        return this;
    }

    public Regimen getRegimen() {
        return Regimen.ofCode(regimen);
    }

    public AccountingPeriod setRegimen(Regimen regimen) {
        this.regimen = regimen.code;
        return this;
    }

    @Override
    public int compareTo(AccountingPeriod that) {
        return new CompareToBuilder().append(this.lower, that.lower).toComparison();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("code", code)
                .toString();
    }

    public enum Regimen {

        Tradicional("RTT"),
        Simplificado("RTS"),
        EspecialAgropecuario("REA");

        private static Map<String, Regimen> byCode;

        static {
            byCode = new TreeMap<>();
            for (Regimen regimen : values()) {
                byCode.put(regimen.code, regimen);
            }
        }

        public final String code;

        Regimen(String code) {
            this.code = code;
        }

        public static Regimen ofCode(String code) {
            return byCode.get(code);
        }

    }
}

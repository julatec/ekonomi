package name.julatec.ekonomi.accounting;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * And accounting period is the time related to a tax regime.
 */
@Entity(name = AccountingPeriod.EXPORT_NAME)
public class AccountingPeriod implements Comparable<AccountingPeriod> {

    /**
     * Name used for external systems such as the database and expert system predicates.
     */
    public static final String EXPORT_NAME = "periods";

    /**
     * Unique identifier, for example RTT-2020, IVA-202007
     */
    @Id
    private String code;

    /**
     * Title.
     */
    private String name;

    /**
     * Kind of Acountind period according the the tax type.
     */
    @Convert(converter = RegimenConverter.class)
    private Regimen regimen;

    /**
     * Initial Date.
     */
    private Date lower;

    /**
     * Final Date.
     */
    private Date upper;

    /**
     * Gets the code, unique identifier of the accounting period.
     *
     * @return code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the code, unique identifier of the accounting period.
     *
     * @param code unique identifier of the accounting period.
     * @return this instance.
     */
    public AccountingPeriod setCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Gets the first date of the account period.
     *
     * @return first date of the account period.
     */
    public Date getLower() {
        return lower;
    }

    /**
     * Sets the first date of the account period.
     *
     * @param lower the first date of the account period.
     * @return this instance.
     */
    public AccountingPeriod setLower(Date lower) {
        this.lower = lower;
        return this;
    }

    /**
     * Gets the last day of the accounting period.
     *
     * @return last day of the accounting period.
     */
    public Date getUpper() {
        return upper;
    }

    /**
     * Sets the last day of the accounting period.
     *
     * @param upper last day of the accounting period.
     * @return this instance.
     */
    public AccountingPeriod setUpper(Date upper) {
        this.upper = upper;
        return this;
    }

    /**
     * Gets the readble name of the accounting period.
     *
     * @return readble name of the accounting period.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the readble name of the accounting period.
     *
     * @param name readble name of the accounting period.
     * @return this instance.
     */
    public AccountingPeriod setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the regimen of the accouting period.
     *
     * @return regimen of the accouting period.
     */
    public Regimen getRegimen() {
        return regimen;
    }

    /**
     * Sets the regimen of the accouting period.
     *
     * @param regimen regimen of the accouting period.
     * @return this instance.
     */
    public AccountingPeriod setRegimen(Regimen regimen) {
        this.regimen = regimen;
        return this;
    }

    /**
     * Compares this accounting period with the that account period based on the end date.
     *
     * @param that accounting period to be compared.
     * @return standard comparisson result.
     */
    @Override
    public int compareTo(AccountingPeriod that) {
        return new CompareToBuilder().append(this.upper, that.upper).toComparison();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("code", code)
                .toString();
    }

    /**
     * Costa Rica has multiple Regime to pay taxes. These are some the most common Regime that a costarican migth have to
     * present an information.
     */
    public enum Regimen {

        /**
         * @see <a href="http://www.pgrweb.go.cr/scij/Busqueda/Normativa/Normas/nrm_texto_completo.aspx?nValor1=1&nValor2=87720">
         * Ley sore el impuesto al valor agregado</a>
         */
        ValorAgreagado("IVA"),
        /**
         * @see <a href="https://www.hacienda.go.cr/contenido/14851-confeccion-presentacion-y-pago-del-impuesto-sobre-la-renta">
         * Impuesto sobre la renta</a>
         */
        Tradicional("RTT"),
        /**
         * @see <a href="https://www.hacienda.go.cr/contenido/14852-informacion-general-regimen-de-tributacion-simplificada">
         * Información general Régimen de Tributación Simplificada</a>
         */
        Simplificado("RTS"),
        /**
         * @see <a href="https://www.hacienda.go.cr/contenido/15455-generalidades-del-regimen-especial-agropecuario-rea-del-impuesto-sobre-el-valor-agregado-iva">
         * Régimen especial agropecuario (REA del Impuesto sobre el Valor Agregado (IVA) </a>
         */
        EspecialAgropecuario("REA");

        /**
         * Reverse map by code.
         */
        private static final Map<String, Regimen> byCode;

        static {
            byCode = new TreeMap<>();
            for (Regimen regimen : values()) {
                byCode.put(regimen.code, regimen);
            }
        }

        /**
         * Code to identify each Regime.
         */
        public final String code;

        /**
         * Constructor
         *
         * @param code to be identified.
         */
        Regimen(String code) {
            this.code = code;
        }

        /**
         * Reverse lookup using the code.
         *
         * @param code to lookup
         * @return the regime associated with given code.
         */
        public static Regimen ofCode(String code) {
            if (code == null) {
                return null;
            }
            return byCode.get(code);
        }

    }

    /**
     * Converts from String to Regime.
     */
    public static final class RegimenConverter implements AttributeConverter<Regimen, String> {

        @Override
        public String convertToDatabaseColumn(Regimen attribute) {
            return attribute.code;
        }

        @Override
        public Regimen convertToEntityAttribute(String dbData) {
            return Regimen.ofCode(dbData);
        }
    }
}


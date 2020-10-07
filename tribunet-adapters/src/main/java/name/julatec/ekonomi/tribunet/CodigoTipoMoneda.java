package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

/**
 * Adapter interface for Currency Code.
 */
@Adapt(value = {
        cr.go.hacienda.tribunet.v43.factura.CodigoMonedaType.class,
        cr.go.hacienda.tribunet.v43.factura.compra.CodigoMonedaType.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.CodigoMonedaType.class,
        cr.go.hacienda.tribunet.v43.tiquete.CodigoMonedaType.class,
        cr.go.hacienda.tribunet.v43.nota.credito.CodigoMonedaType.class,
        cr.go.hacienda.tribunet.v43.nota.debito.CodigoMonedaType.class,
})
public interface CodigoTipoMoneda {

    /**
     * In case the Currency code the defaults is CRC.
     */
    String CRC = "CRC";

    /**
     * Default Currency.
     */
    Currency CURRENCY = Currency.getInstance(CRC);

    /**
     * Default exchange rate to CRC with 1 as factor.
     */
    CodigoTipoMoneda DEFAULT = new CodigoTipoMonedaImpl(CURRENCY, ONE);

    /**
     * Gets Currency Code.
     *
     * @return Currency Code.
     */
    default String getCodigoMoneda() {
        return null;
    }

    /**
     * Gets the exchange rate from CRC to the given currency Code.
     *
     * @return Exchange rate.
     */
    default BigDecimal getTipoCambio() {
        return null;
    }

    /**
     * Currency loaded from the code.
     *
     * @return currency.
     */
    default Currency getCurrency() {
        return Currency.getInstance(getCodigoMoneda());
    }

    /**
     * Provides a cannonical instance equivalent to this.
     *
     * @return cannonical representatation.
     */
    default CodigoTipoMoneda cannonical() {
        final String currency = Optional.ofNullable(getCodigoMoneda()).orElse(CRC);
        if (CRC.equals(currency)) {
            return DEFAULT;
        }
        final BigDecimal exchangeRate = getTipoCambio();
        if (exchangeRate == null || ONE.compareTo(exchangeRate) == 0 || ZERO.compareTo(exchangeRate) == 0) {
            return DEFAULT;
        }
        return new CodigoTipoMonedaImpl(Currency.getInstance(currency), exchangeRate);
    }

}

/**
 * Simple implementation of CodigoTipoMoneda. Useful for the {@link CodigoTipoMoneda#cannonical()} method.
 */
final class CodigoTipoMonedaImpl implements CodigoTipoMoneda {

    private final Currency currency;
    private final BigDecimal exchangeRate;

    /**
     * Default constructor.
     *
     * @param currency
     * @param exchangeRate
     */
    CodigoTipoMonedaImpl(Currency currency, BigDecimal exchangeRate) {
        this.currency = currency;
        this.exchangeRate = exchangeRate;
    }

    @Override
    public String getCodigoMoneda() {
        return currency.getCurrencyCode();
    }

    @Override
    public BigDecimal getTipoCambio() {
        return exchangeRate;
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CodigoTipoMonedaImpl that = (CodigoTipoMonedaImpl) o;

        return new EqualsBuilder()
                .append(currency, that.currency)
                .append(exchangeRate, that.exchangeRate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(currency)
                .append(exchangeRate)
                .toHashCode();
    }
}

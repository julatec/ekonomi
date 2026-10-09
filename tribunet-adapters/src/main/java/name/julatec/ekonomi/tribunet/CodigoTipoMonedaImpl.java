package name.julatec.ekonomi.tribunet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.util.Currency;

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

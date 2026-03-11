package name.julatec.ekonomi.tribunet;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class CodigoTipoMonedaImplTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency CRC = Currency.getInstance("CRC");
    private static final BigDecimal RATE = BigDecimal.valueOf(560.0);

    private CodigoTipoMoneda usdImpl() {
        return new CodigoTipoMoneda() {
            @Override
            public String getCodigoMoneda() { return "USD"; }
            @Override
            public BigDecimal getTipoCambio() { return RATE; }
        }.cannonical();
    }

    @Test
    void cannonical_usd_getCodigoMoneda_returnsUSD() {
        CodigoTipoMoneda impl = usdImpl();
        assertEquals("USD", impl.getCodigoMoneda());
    }

    @Test
    void cannonical_usd_getTipoCambio_returnsRate() {
        CodigoTipoMoneda impl = usdImpl();
        assertEquals(RATE, impl.getTipoCambio());
    }

    @Test
    void cannonical_usd_getCurrency_returnsUSD() {
        CodigoTipoMoneda impl = usdImpl();
        assertEquals(USD, impl.getCurrency());
    }

    @Test
    void codigoTipoMoneda_default_getCurrency_usesCurrencyInstanceOfCode() {
        CodigoTipoMoneda c = new CodigoTipoMoneda() {
            @Override
            public String getCodigoMoneda() { return "USD"; }
        };
        assertEquals(USD, c.getCurrency());
    }

    @Test
    void default_getCurrency_CRC_returnsCrcCurrency() {
        CodigoTipoMoneda c = new CodigoTipoMoneda() {
            @Override
            public String getCodigoMoneda() { return "CRC"; }
        };
        assertEquals(CRC, c.getCurrency());
    }

    @Test
    void cannonical_DEFAULT_getCurrency_returnsCRC() {
        assertEquals(CRC, CodigoTipoMoneda.DEFAULT.getCurrency());
    }

    @Test
    void cannonical_DEFAULT_getCodigoMoneda_returnsCRC() {
        assertEquals("CRC", CodigoTipoMoneda.DEFAULT.getCodigoMoneda());
    }

    @Test
    void cannonical_DEFAULT_getTipoCambio_returnsOne() {
        assertEquals(0, BigDecimal.ONE.compareTo(CodigoTipoMoneda.DEFAULT.getTipoCambio()));
    }

    // ---- equals/hashCode on CodigoTipoMonedaImpl (via cannonical result) ----

    @Test
    void cannonical_usd_equals_sameValues_isTrue() {
        CodigoTipoMoneda a = usdImpl();
        CodigoTipoMoneda b = usdImpl();
        assertEquals(a, b);
    }

    @Test
    void cannonical_usd_equals_sameInstance_isTrue() {
        CodigoTipoMoneda a = usdImpl();
        assertEquals(a, a);
    }

    @Test
    void cannonical_usd_equals_null_isFalse() {
        CodigoTipoMoneda a = usdImpl();
        assertNotEquals(null, a);
    }

    @Test
    void cannonical_usd_equals_string_isFalse() {
        CodigoTipoMoneda a = usdImpl();
        assertNotEquals("USD", a);
    }

    @Test
    void cannonical_usd_equals_differentRate_isFalse() {
        CodigoTipoMoneda a = usdImpl(); // rate 560
        CodigoTipoMoneda b = new CodigoTipoMoneda() {
            @Override
            public String getCodigoMoneda() { return "USD"; }
            @Override
            public BigDecimal getTipoCambio() { return BigDecimal.valueOf(600.0); }
        }.cannonical();
        assertNotEquals(a, b);
    }

    @Test
    void cannonical_usd_equals_differentCurrency_isFalse() {
        CodigoTipoMoneda a = usdImpl();
        CodigoTipoMoneda b = new CodigoTipoMoneda() {
            @Override
            public String getCodigoMoneda() { return "EUR"; }
            @Override
            public BigDecimal getTipoCambio() { return RATE; }
        }.cannonical();
        assertNotEquals(a, b);
    }

    @Test
    void cannonical_usd_hashCode_sameValues_equal() {
        CodigoTipoMoneda a = usdImpl();
        CodigoTipoMoneda b = usdImpl();
        assertEquals(a.hashCode(), b.hashCode());
    }
}

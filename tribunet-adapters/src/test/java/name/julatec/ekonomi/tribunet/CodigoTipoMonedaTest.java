package name.julatec.ekonomi.tribunet;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class CodigoTipoMonedaTest {

    @Test
    void cannonicalFromNull() {
        CodigoTipoMoneda codigoTipoMoneda = new CodigoTipoMoneda() {
            @Override
            public String getCodigoMoneda() {
                return null;
            }

            @Override
            public BigDecimal getTipoCambio() {
                return null;
            }
        };
        assertEquals(CodigoTipoMoneda.DEFAULT,  codigoTipoMoneda.cannonical());
    }

    @Test
    void cannonicalFromCRC() {
        CodigoTipoMoneda codigoTipoMoneda = new CodigoTipoMoneda() {
            @Override
            public String getCodigoMoneda() {
                return "CRC";
            }

            @Override
            public BigDecimal getTipoCambio() {
                return null;
            }
        };
        assertEquals(CodigoTipoMoneda.DEFAULT,  codigoTipoMoneda.cannonical());
    }

    @Test
    void cannonicalFromUSD() {
        CodigoTipoMoneda codigoTipoMoneda = new CodigoTipoMoneda() {
            @Override
            public String getCodigoMoneda() {
                return "CRC";
            }

            @Override
            public BigDecimal getTipoCambio() {
                return BigDecimal.ONE;
            }
        };
        assertEquals(CodigoTipoMoneda.DEFAULT,  codigoTipoMoneda.cannonical());
    }

    @Test
    void cannonicalFromUSD2() {
        CodigoTipoMoneda codigoTipoMoneda = new CodigoTipoMoneda() {
            @Override
            public String getCodigoMoneda() {
                return "CRC";
            }

            @Override
            public BigDecimal getTipoCambio() {
                return BigDecimal.ZERO;
            }
        };
        assertEquals(CodigoTipoMoneda.DEFAULT,  codigoTipoMoneda.cannonical());
    }


    @Test
    void cannonicalFromCRC3() {
        CodigoTipoMoneda codigoTipoMoneda = new CodigoTipoMoneda() {
            @Override
            public String getCodigoMoneda() {
                return "CRC";
            }

            @Override
            public BigDecimal getTipoCambio() {
                return BigDecimal.valueOf(560.0d);
            }
        };
        assertEquals(CodigoTipoMoneda.DEFAULT,  codigoTipoMoneda.cannonical());
    }

    @Test
    void cannonicalFromUSD4() {
        CodigoTipoMoneda codigoTipoMoneda = new CodigoTipoMoneda() {
            @Override
            public String getCodigoMoneda() {
                return "USD";
            }

            @Override
            public BigDecimal getTipoCambio() {
                return BigDecimal.valueOf(560.0d);
            }
        };
        assertEquals(new CodigoTipoMonedaImpl(Currency.getInstance("USD"), BigDecimal.valueOf(560.0d)),
                codigoTipoMoneda.cannonical());
    }
}
package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.math.BigDecimal;

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
     * Gets Currency Code.
     * @return Currency Code.
     */
    default String getCodigoMoneda() {
        return null;
    }

    /**
     * Gets the exchange rate from CRC to the given currency Code.
     * @return Exchange rate.
     */
    default BigDecimal getTipoCambio() {
        return null;
    }

}

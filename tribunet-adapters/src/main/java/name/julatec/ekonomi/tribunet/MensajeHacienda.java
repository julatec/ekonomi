package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.math.BigDecimal;

@Adapt({
        cr.go.hacienda.tribunet.v42y2016.mensaje.hacienda.MensajeHacienda.class,
        cr.go.hacienda.tribunet.v42y2017.mensaje.hacienda.MensajeHacienda.class,
        cr.go.hacienda.tribunet.v43.mensaje.hacienda.MensajeHacienda.class,
})
public interface MensajeHacienda {

    /**
     * Document unique identifier.
     *
     * @return document unique identifier.
     */
    String getClave();

    /**
     * Document emitter unique identifier.
     *
     * @return document emitter unique identifier.
     */
    String getNumeroCedulaEmisor();

    /**
     * Total tax amount.
     *
     * @return total tax amount.
     */
    BigDecimal getMontoTotalImpuesto();

    /**
     * Document total.
     *
     * @return document total.
     */
    BigDecimal getTotalFactura();

}

package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.math.BigDecimal;

@Adapt({

        /* Verison 4.2 2016 */

        cr.go.hacienda.tribunet.v42y2016.mensaje.hacienda.MensajeHacienda.class,

        /* Verison 4.2 2017 */

        cr.go.hacienda.tribunet.v42y2017.mensaje.hacienda.MensajeHacienda.class,

        /* Verison 4.3 */

        cr.go.hacienda.tribunet.v43.mensaje.hacienda.MensajeHacienda.class,

        /* Verison 4.4 */

        cr.go.hacienda.tribunet.v44.mensaje.hacienda.MensajeHacienda.class,

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

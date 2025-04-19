package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

@Adapt({

        /* Verison 4.2 2016 */

        cr.go.hacienda.tribunet.v42y2016.factura.CodigoType.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.CodigoType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.credito.CodigoType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.debito.CodigoType.class,

        /* Verison 4.2 2017 */

        cr.go.hacienda.tribunet.v42y2017.factura.CodigoType.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.CodigoType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.credito.CodigoType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.debito.CodigoType.class,

        /* Verison 4.3 */

        cr.go.hacienda.tribunet.v43.factura.CodigoType.class,
        cr.go.hacienda.tribunet.v43.factura.compra.CodigoType.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.CodigoType.class,
        cr.go.hacienda.tribunet.v43.tiquete.CodigoType.class,
        cr.go.hacienda.tribunet.v43.nota.credito.CodigoType.class,
        cr.go.hacienda.tribunet.v43.nota.debito.CodigoType.class,

        /* Verison 4.4 */

        cr.go.hacienda.tribunet.v44.factura.CodigoType.class,
        cr.go.hacienda.tribunet.v44.factura.compra.CodigoType.class,
        cr.go.hacienda.tribunet.v44.factura.exportacion.CodigoType.class,
        cr.go.hacienda.tribunet.v44.tiquete.CodigoType.class,
        cr.go.hacienda.tribunet.v44.nota.credito.CodigoType.class,
        cr.go.hacienda.tribunet.v44.nota.debito.CodigoType.class,
})

public interface CodigoType {









}

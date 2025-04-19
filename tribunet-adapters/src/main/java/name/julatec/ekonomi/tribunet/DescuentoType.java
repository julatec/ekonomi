package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

@Adapt({

        /* Verison 4.3 */

        cr.go.hacienda.tribunet.v43.factura.DescuentoType.class,
        cr.go.hacienda.tribunet.v43.factura.compra.DescuentoType.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.DescuentoType.class,
        cr.go.hacienda.tribunet.v43.tiquete.DescuentoType.class,
        cr.go.hacienda.tribunet.v43.nota.credito.DescuentoType.class,
        cr.go.hacienda.tribunet.v43.nota.debito.DescuentoType.class,

        /* Verison 4.4 */

        cr.go.hacienda.tribunet.v44.factura.DescuentoType.class,
        cr.go.hacienda.tribunet.v44.factura.compra.DescuentoType.class,
        cr.go.hacienda.tribunet.v44.factura.exportacion.DescuentoType.class,
        cr.go.hacienda.tribunet.v44.tiquete.DescuentoType.class,
        cr.go.hacienda.tribunet.v44.nota.credito.DescuentoType.class,
        cr.go.hacienda.tribunet.v44.nota.debito.DescuentoType.class,
})

public interface DescuentoType {
}

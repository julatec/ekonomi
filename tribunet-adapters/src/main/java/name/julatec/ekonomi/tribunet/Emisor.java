package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

/**
 * Emitter Adapter Interface.
 */
@Adapt({

        /*<Version 4.2 2016>*/

        cr.go.hacienda.tribunet.v42y2016.factura.EmisorType.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.EmisorType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.credito.EmisorType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.debito.EmisorType.class,

        /*<Version 4.2 2017>*/

        cr.go.hacienda.tribunet.v42y2017.factura.EmisorType.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.EmisorType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.credito.EmisorType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.debito.EmisorType.class,

        /*<Version 4.3>*/

        cr.go.hacienda.tribunet.v43.factura.EmisorType.class,
        cr.go.hacienda.tribunet.v43.factura.compra.EmisorType.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.EmisorType.class,
        cr.go.hacienda.tribunet.v43.tiquete.EmisorType.class,
        cr.go.hacienda.tribunet.v43.nota.credito.EmisorType.class,
        cr.go.hacienda.tribunet.v43.nota.debito.EmisorType.class,

        /*<Version 4.4>*/

        cr.go.hacienda.tribunet.v44.factura.EmisorType.class,
        cr.go.hacienda.tribunet.v44.factura.compra.EmisorType.class,
        cr.go.hacienda.tribunet.v44.factura.exportacion.EmisorType.class,
        cr.go.hacienda.tribunet.v44.tiquete.EmisorType.class,
        cr.go.hacienda.tribunet.v44.nota.credito.EmisorType.class,
        cr.go.hacienda.tribunet.v44.nota.debito.EmisorType.class,


})
public interface Emisor {

    /**
     * Name of the document emitter.
     *
     * @return name of the document emitter.
     */
    String getNombre();

    /**
     * Identifier fo the document emitter.
     *
     * @return identifier fo the document emitter.
     */
    Identificacion getIdentificacion();

}

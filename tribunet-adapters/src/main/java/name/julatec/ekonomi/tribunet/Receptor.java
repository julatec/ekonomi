package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

/**
 * Receptor Adapter Interface.
 */
@Adapt({
        cr.go.hacienda.tribunet.v42y2016.factura.ReceptorType.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.ReceptorType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.credito.ReceptorType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.debito.ReceptorType.class,
        cr.go.hacienda.tribunet.v42y2017.factura.ReceptorType.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.ReceptorType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.credito.ReceptorType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.debito.ReceptorType.class,
        cr.go.hacienda.tribunet.v43.factura.ReceptorType.class,
        cr.go.hacienda.tribunet.v43.factura.compra.ReceptorType.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.ReceptorType.class,
        cr.go.hacienda.tribunet.v43.tiquete.ReceptorType.class,
        cr.go.hacienda.tribunet.v43.nota.credito.ReceptorType.class,
        cr.go.hacienda.tribunet.v43.nota.debito.ReceptorType.class,
})
public interface Receptor {

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

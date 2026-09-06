package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.util.Date;
import java.util.stream.Stream;

/**
 * Factura adapter interface.
 */
@Adapt({

        /* Verison 4.2 2016 */

        cr.go.hacienda.tribunet.v42y2016.factura.FacturaElectronica.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.TiqueteElectronico.class,

        /* Verison 4.2 2017 */

        cr.go.hacienda.tribunet.v42y2017.factura.FacturaElectronica.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.TiqueteElectronico.class,

    /* Verison 4.3 */

        cr.go.hacienda.tribunet.v43.factura.FacturaElectronica.class,
        cr.go.hacienda.tribunet.v43.tiquete.TiqueteElectronico.class,

        /*Verison 4.4*/

        cr.go.hacienda.tribunet.v44.factura.FacturaElectronica.class,
        cr.go.hacienda.tribunet.v44.tiquete.TiqueteElectronico.class

})
public interface Factura extends Documento {

    /**
     * Document unique identifier.
     *
     * @return document unique identifier.
     */
    String getClave();

    /**
     * Document emitter consecutive number.
     *
     * @return document emitter consecutive number.
     */
    String getNumeroConsecutivo();

    /**
     * Document emitter.
     *
     * @return document emitter.
     */
    Emisor getEmisor();

    /**
     * Document receptor.
     *
     * @return document receptor.
     */
    Receptor getReceptor();

    /**
     * Document summary.
     *
     * @return document summary.
     */
    Resumen getResumenFactura();

    /**
     * Document details.
     *
     * @return document details.
     */
    DetalleServicio getDetalleServicio();

    /**
     * Emission date.
     *
     * @return Emission date as {@link javax.xml.datatype.XMLGregorianCalendar}.
     */
    javax.xml.datatype.XMLGregorianCalendar getFechaEmision();

    /**
     * Emission date.
     *
     * @return Emission date as {@link java.util.Date}.
     */
    default Date getFechaEmisionAsDate() {
        return getFechaEmision().toGregorianCalendar().getTime();
    }

    /**
     * Informacion Referencia.
     *
     * @return informacion referencia.
     */
    Stream<InformacionReferencia> getInformacionReferencia();

    /** v4.3: único campo. Ver {@link Documento#getCodigoActividad()}. */
    default String getCodigoActividad() {
        return null;
    }

    /** v4.4: el del emisor. */
    default String getCodigoActividadEmisor() {
        return null;
    }

    /** v4.4: el del receptor, cuando el documento lo trae. */
    default String getCodigoActividadReceptor() {
        return null;
    }
}

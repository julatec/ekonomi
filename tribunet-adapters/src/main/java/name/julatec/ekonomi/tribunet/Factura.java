package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.util.Date;
import java.util.stream.Stream;

/**
 * Factura adapter interface.
 */
@Adapt({
        cr.go.hacienda.tribunet.v42y2016.factura.FacturaElectronica.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.TiqueteElectronico.class,
        cr.go.hacienda.tribunet.v42y2017.factura.FacturaElectronica.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.TiqueteElectronico.class,
        cr.go.hacienda.tribunet.v43.factura.FacturaElectronica.class,
        cr.go.hacienda.tribunet.v43.tiquete.TiqueteElectronico.class,
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
}

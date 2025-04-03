package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.util.Date;
import java.util.stream.Stream;

/**
 * Nota Credito adapter interface.
 */
@Adapt({

        /* Verison 4.2 2016 */

        cr.go.hacienda.tribunet.v42y2016.nota.credito.NotaCreditoElectronica.class,

        /* Verison 4.2 2017 */

        cr.go.hacienda.tribunet.v42y2017.nota.credito.NotaCreditoElectronica.class,

        /* Verison 4.3 */

        cr.go.hacienda.tribunet.v43.nota.credito.NotaCreditoElectronica.class,

        /* Verison 4.4 */

        cr.go.hacienda.tribunet.v44.nota.credito.NotaCreditoElectronica.class,
})
public interface NotaCredito extends Documento {

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

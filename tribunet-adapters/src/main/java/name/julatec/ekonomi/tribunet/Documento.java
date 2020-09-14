package name.julatec.ekonomi.tribunet;

import java.util.Date;
import java.util.stream.Stream;

/**
 * Base interface for Electronic Documents.
 */
public interface Documento {

    /**
     * Unique identifier for a Document.
     *
     * @return Unique ID.
     */
    String getClave();

    /**
     * Consecutive number for the Emitter.
     *
     * @return Emitter consecutive number.
     */
    String getNumeroConsecutivo();

    /**
     * Emission date.
     *
     * @return Emission date as {@link javax.xml.datatype.XMLGregorianCalendar}.
     */
    javax.xml.datatype.XMLGregorianCalendar getFechaEmision();

    /**
     * Document Emitter.
     *
     * @return emitter.
     */
    Emisor getEmisor();

    /**
     * Document Receptor.
     *
     * @return receptor.
     */
    Receptor getReceptor();

    /**
     * Document Summary.
     *
     * @return document Summary.
     */
    Resumen getResumenFactura();

    /**
     * Document Details.
     *
     * @return document details.
     */
    DetalleServicio getDetalleServicio();

    /**
     * Informacion Referencia.
     *
     * @return informacion referencia.
     */
    Stream<InformacionReferencia> getInformacionReferencia();

    /**
     * Emission date.
     *
     * @return Emission date as {@link java.util.Date}.
     */
    default Date getFechaEmisionAsDate() {
        return getFechaEmision().toGregorianCalendar().getTime();
    }

}

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

    /**
     * Código de actividad económica, como lo trae la v4.3 — un único campo, sin distinguir
     * de quién es.
     * <p>
     * v4.2 (2016/2017) no tenía este campo: default {@code null}, igual que el resto de lo
     * que varía por versión. v4.4 lo partió en {@link #getCodigoActividadEmisor()} y
     * {@link #getCodigoActividadReceptor()}; este getter sigue existiendo para que las
     * facturas de esa era —que son la mayoría del histórico— no se queden sin código.
     * <p>
     * Cada interfaz concreta (@code Factura}, {@code NotaCredito}, etc.) tiene que
     * redeclarar este getter para que el generador de adaptadores lo procese: solo mira los
     * métodos declarados directamente en la interfaz que está adaptando, no los heredados.
     */
    default String getCodigoActividad() {
        return null;
    }

    /**
     * Código de actividad económica del emisor, como lo trae la v4.4.
     *
     * @see #getCodigoActividadEmisorEfectivo()
     */
    default String getCodigoActividadEmisor() {
        return null;
    }

    /**
     * Código de actividad económica del receptor, cuando el documento lo trae.
     * <p>
     * Existe solo en v4.4, y ahí es opcional en casi todos los tipos —obligatorio nada más
     * en la factura de compra, donde el receptor es quien declara el gasto—.
     */
    default String getCodigoActividadReceptor() {
        return null;
    }

    /**
     * El código de actividad del EMISOR, sea cual sea la versión que lo trajo.
     * <p>
     * v4.3 trae un único campo sin distinguir de quién es, y en ese entonces siempre era el
     * del emisor: la inversión de roles en la factura de compra —donde quien firma y
     * transmite es el receptor— es una particularidad que introdujo v4.4, con su propio
     * campo separado. Si algún día un documento trajera los dos, gana el explícito de v4.4.
     */
    default String getCodigoActividadEmisorEfectivo() {
        return getCodigoActividadEmisor() != null ? getCodigoActividadEmisor() : getCodigoActividad();
    }

}

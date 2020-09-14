package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Exoneration type Adapter.
 */
@Adapt({
        cr.go.hacienda.tribunet.v42y2016.factura.ExoneracionType.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.ExoneracionType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.credito.ExoneracionType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.debito.ExoneracionType.class,
        cr.go.hacienda.tribunet.v42y2017.factura.ExoneracionType.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.ExoneracionType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.credito.ExoneracionType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.debito.ExoneracionType.class,
        cr.go.hacienda.tribunet.v43.factura.ExoneracionType.class,
        cr.go.hacienda.tribunet.v43.factura.compra.ExoneracionType.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.ExoneracionType.class,
        cr.go.hacienda.tribunet.v43.tiquete.ExoneracionType.class,
        cr.go.hacienda.tribunet.v43.nota.credito.ExoneracionType.class,
        cr.go.hacienda.tribunet.v43.nota.debito.ExoneracionType.class,
})
public interface ExoneracionType {

    /**
     * Document type.
     *
     * @return document type.
     */
    default String getTipoDocumento() {
        return null;
    }

    /**
     * Document number.
     *
     * @return document number.
     */
    default String getNumeroDocumento() {
        return null;
    }

    /**
     * Institution name.
     *
     * @return institution name.
     */
    default String getNombreInstitucion() {
        return null;
    }

    /**
     * Emission date.
     *
     * @return emission date as {@link XMLGregorianCalendar}.
     */
    default XMLGregorianCalendar getFechaEmision() {
        return null;
    }

    /**
     * Emission date.
     *
     * @return Emission date as {@link java.util.Date}.
     */
    default Date getFechaEmisionAsDate() {
        return getFechaEmision().toGregorianCalendar().getTime();
    }

    /**
     * Exoneration percentage.
     *
     * @return exoneration percentage.
     */
    default BigInteger getPorcentajeExoneracion() {
        return null;
    }

    /**
     * Exoneration amount.
     *
     * @return exoneration amount.
     */
    default BigDecimal getMontoExoneracion() {
        return null;
    }

}

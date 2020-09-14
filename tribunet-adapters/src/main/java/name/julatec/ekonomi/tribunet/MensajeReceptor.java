package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

/**
 * Mensaje Hacienda adapter interface
 */
@Adapt({
        cr.go.hacienda.tribunet.v42y2016.mensaje.receptor.MensajeReceptor.class,
        cr.go.hacienda.tribunet.v42y2017.mensaje.receptor.MensajeReceptor.class,
        cr.go.hacienda.tribunet.v43.mensaje.receptor.MensajeReceptor.class,
})
public interface MensajeReceptor {

    /**
     * Document unique identifier.
     *
     * @return document unique identifier.
     */
    String getClave();

    /**
     * Emission date.
     *
     * @return Emission date as {@link javax.xml.datatype.XMLGregorianCalendar}.
     */
    XMLGregorianCalendar getFechaEmisionDoc();


    /**
     * Emission date.
     *
     * @return Emission date as {@link java.util.Date}.
     */
    default Date getFechaEmisionDocAsDate() {
        return Optional.ofNullable(getFechaEmisionDoc())
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .map(Calendar::getTime)
                .orElse(null);
    }

    /**
     * Document emitter unique identifier.
     *
     * @return document emitter unique identifier.
     */
    String getNumeroCedulaEmisor();

    /**
     * Document receptor unique identifier.
     *
     * @return document receptor unique identifier.
     */
    String getNumeroCedulaReceptor();

    /**
     * Total tax amount.
     *
     * @return total tax amount.
     */
    BigDecimal getMontoTotalImpuesto();

    /**
     * Document total.
     *
     * @return document total.
     */
    BigDecimal getTotalFactura();

}

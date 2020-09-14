package name.julatec.ekonomi.tribunet.storage;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "mensaje")
@Table(indexes = {
        @Index(name = "emisor_numero_index", columnList = "emisor_numero"),
        @Index(name = "receptor_numero_index", columnList = "receptor_numero"),
        @Index(name = "emisor_receptor_index", columnList = "emisor_numero,receptor_numero"),
        @Index(name = "fecha_emision_index", columnList = "receptor_fecha"),
})
public class Mensaje {

    @Id
    @Column(name = "clave")
    protected String clave;

    @Column(name = "emisor_numero")
    protected String emisorNumero;

    @Column(name = "receptor_numero")
    protected String receptorNumero;

    @Column(name = "total_impuesto")
    protected BigDecimal totalImpuesto;

    @Column(name = "total_comprobante")
    protected BigDecimal totalComprobante;

    @Column(name = "receptor_fecha")
    protected Date receptorFecha;

    @Lob
    @Column(name = "mensaje_hacienda")
    protected String mensajeHacienda;

    @Lob
    @Column(name = "mensaje_receptor")
    protected String mensajeReceptor;

    public String getClave() {
        return clave;
    }

    public Mensaje setClave(String clave) {
        this.clave = clave;
        return this;
    }

    public String getEmisorNumero() {
        return emisorNumero;
    }

    public Mensaje setEmisorNumero(String emisorNumero) {
        this.emisorNumero = emisorNumero;
        return this;
    }

    public String getReceptorNumero() {
        return receptorNumero;
    }

    public Mensaje setReceptorNumero(String receptorNumero) {
        this.receptorNumero = receptorNumero;
        return this;
    }

    public BigDecimal getTotalImpuesto() {
        return totalImpuesto;
    }

    public Mensaje setTotalImpuesto(BigDecimal totalImpuesto) {
        this.totalImpuesto = totalImpuesto;
        return this;
    }

    public BigDecimal getTotalComprobante() {
        return totalComprobante;
    }

    public Mensaje setTotalComprobante(BigDecimal totalComprobante) {
        this.totalComprobante = totalComprobante;
        return this;
    }

    public Date getReceptorFecha() {
        return receptorFecha;
    }

    public Mensaje setReceptorFecha(Date receptorFecha) {
        this.receptorFecha = receptorFecha;
        return this;
    }

    public String getMensajeHacienda() {
        return mensajeHacienda;
    }

    public Mensaje setMensajeHacienda(String mensajeHacienda) {
        this.mensajeHacienda = mensajeHacienda;
        return this;
    }

    public String getMensajeReceptor() {
        return mensajeReceptor;
    }

    public Mensaje setMensajeReceptor(String mensajeReceptor) {
        this.mensajeReceptor = mensajeReceptor;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Mensaje mensaje = (Mensaje) o;

        return new EqualsBuilder()
                .append(clave, mensaje.clave)
                .append(emisorNumero, mensaje.emisorNumero)
                .append(receptorNumero, mensaje.receptorNumero)
                .append(totalImpuesto, mensaje.totalImpuesto)
                .append(totalComprobante, mensaje.totalComprobante)
                .append(receptorFecha, mensaje.receptorFecha)
                .append(mensajeHacienda, mensaje.mensajeHacienda)
                .append(mensajeReceptor, mensaje.mensajeReceptor)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(clave)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("clave", clave)
                .append("emisorNumero", emisorNumero)
                .append("receptorNumero", receptorNumero)
                .append("totalImpuesto", totalImpuesto)
                .append("totalComprobante", totalComprobante)
                .append("receptorFecha", receptorFecha)
                .toString();
    }


}

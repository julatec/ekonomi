package name.julatec.ekonomi.tribunet.storage;

import name.julatec.ekonomi.accounting.Record;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Currency;
import java.util.Optional;

@Entity(name = "nota_debito")
@Table(indexes = {
        @Index(name = "emisor_numero_index", columnList = "emisor_numero"),
        @Index(name = "receptor_numero_index", columnList = "receptor_numero"),
        @Index(name = "numero_consecutivo_index", columnList = "numero_consecutivo"),
        @Index(name = "emisor_receptor_index", columnList = "emisor_numero,receptor_numero"),
        @Index(name = "fecha_emision_index", columnList = "fecha_emision"),
})
public class NotaDebito implements ElectronicReceipt {

    public static final String EXPORT_NAME = "notaDebito";

    @EmbeddedId
    private ClaveNota claveNota;

    @Embedded
    private Documento documento;

    @Transient
    private final Record.Key key = new Record.Key(
            () -> documento.getFechaEmision(),
            () -> documento.getResumen().getTotalComprobante(),
            () -> Currency.getInstance(documento.getResumen().getCodigoMoneda()));

    public ClaveNota getClaveNota() {
        return claveNota;
    }

    public NotaDebito setClaveNota(ClaveNota claveNota) {
        this.claveNota = claveNota;
        return this;
    }

    public Documento getDocumento() {
        return documento;
    }

    public NotaDebito setDocumento(Documento documento) {
        this.documento = documento;
        return this;
    }

    public String getRecordKey() {
        return getClaveNota().getClave();
    }

    public String getRecordSequential() {
        return getClaveNota().getNumeroConsecutivo();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NotaDebito that = (NotaDebito) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(claveNota, that.claveNota)
                .append(documento, that.documento)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(claveNota)
                .toHashCode();
    }

    @Override
    public Optional<Emisor> getEmisor() {
        return Optional.ofNullable(documento.getEmisor());
    }

    @Override
    public Optional<Receptor> getReceptor() {
        return Optional.ofNullable(documento.getReceptor());
    }

    @Override
    public Record.Key getKey() {
        return key;
    }

    public String getClave() {
        return getClaveNota().clave;
    }

}

package name.julatec.ekonomi.tribunet.storage;

import name.julatec.ekonomi.accounting.Record;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity(name = "factura_exportacion")
@Table(indexes = {
        @Index(name = "emisor_numero_index", columnList = "emisor_numero"),
        @Index(name = "receptor_numero_index", columnList = "receptor_numero"),
        @Index(name = "numero_consecutivo_index", columnList = "numero_consecutivo"),
        @Index(name = "emisor_receptor_index", columnList = "emisor_numero,receptor_numero"),
        @Index(name = "fecha_emision_index", columnList = "fecha_emision"),
})
public class FacturaExportacion implements ElectronicReceipt {

    private static final Currency CURRENCY = Currency.getInstance(Locale.getDefault());
    public static final String EXPORT_NAME = "factura_exportacion";

    @Id
    protected String clave;

    @Column(name = "numero_consecutivo")
    protected String numeroConsecutivo;

    @Embedded
    protected Documento documento;

    @Transient
    private final Record.Key key = new Record.Key(
            () -> documento.getFechaEmision(),
            () -> documento.getResumen().getTotalComprobante(),
            () -> Optional.ofNullable(documento.getResumen().getCodigoMoneda())
                    .map(Currency::getInstance)
                    .orElse(CURRENCY));

    public String getClave() {
        return clave;
    }

    public FacturaExportacion setClave(String clave) {
        this.clave = clave;
        return this;
    }

    public String getNumeroConsecutivo() {
        return numeroConsecutivo;
    }

    public FacturaExportacion setNumeroConsecutivo(String numeroConsecutivo) {
        this.numeroConsecutivo = numeroConsecutivo;
        return this;
    }

    public Documento getDocumento() {
        return documento;
    }

    public FacturaExportacion setDocumento(Documento documento) {
        this.documento = documento;
        return this;
    }

    public String getRecordKey() {
        return getClave();
    }

    public String getRecordSequential() {
        return getNumeroConsecutivo();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FacturaExportacion factura = (FacturaExportacion) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(clave, factura.clave)
                .append(numeroConsecutivo, factura.numeroConsecutivo)
                .append(documento, factura.documento)
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
                .append("numeroConsecutivo", numeroConsecutivo)
                .append("documento", documento)
                .toString();
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

}

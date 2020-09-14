package name.julatec.ekonomi.tribunet.storage;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@SuppressWarnings("JpaDataSourceORMInspection")
@Embeddable
public class ClaveNota implements Serializable {

    @Column(name = "clave")
    protected String clave;

    @Column(name = "numero_consecutivo")
    protected String numeroConsecutivo;

    public String getClave() {
        return clave;
    }

    public ClaveNota setClave(String clave) {
        this.clave = clave;
        return this;
    }

    public String getNumeroConsecutivo() {
        return numeroConsecutivo;
    }

    public ClaveNota setNumeroConsecutivo(String numeroConsecutivo) {
        this.numeroConsecutivo = numeroConsecutivo;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ClaveNota claveNota = (ClaveNota) o;

        return new EqualsBuilder()
                .append(clave, claveNota.clave)
                .append(numeroConsecutivo, claveNota.numeroConsecutivo)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(clave)
                .append(numeroConsecutivo)
                .toHashCode();
    }
}

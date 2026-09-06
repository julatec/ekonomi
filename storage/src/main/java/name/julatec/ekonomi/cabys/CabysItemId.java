package name.julatec.ekonomi.cabys;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 * (versión del catálogo, código) — la llave compuesta de {@link CabysItem}.
 * <p>
 * Compuesta y no {@code codigo} solo, porque el mismo código de 13 dígitos existe en más de
 * una versión del catálogo a la vez: es justo lo que permite validar un comprobante viejo
 * contra el catálogo de la fecha en que se emitió, en vez de contra el vigente hoy.
 */
public class CabysItemId implements Serializable {

    @Column(length = 20)
    private String version;

    @Column(length = 13)
    private String codigo;

    public CabysItemId() {
    }

    public CabysItemId(String version, String codigo) {
        this.version = version;
        this.codigo = codigo;
    }

    public String getVersion() {
        return version;
    }

    public CabysItemId setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getCodigo() {
        return codigo;
    }

    public CabysItemId setCodigo(String codigo) {
        this.codigo = codigo;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CabysItemId that = (CabysItemId) o;
        return new EqualsBuilder()
                .append(version, that.version)
                .append(codigo, that.codigo)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(version)
                .append(codigo)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("version", version)
                .append("codigo", codigo)
                .toString();
    }
}

package name.julatec.ekonomi.preferencias;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * (usuario, reporte) — la llave de {@link PreferenciaColumnas}.
 * <p>
 * Lleva el reporte aunque hoy haya uno solo ({@link ColumnasDeReporte#REPORTE_COMPROBANTES},
 * el auxiliar de compras y ventas) porque el esquema de producción se crea a mano —
 * {@code ddl-auto} es {@code none} contra la base real, ver {@code SecurityConfig}— y agregar
 * una columna a la llave primaria después es una migración; dejarla puesta ahora no cuesta
 * nada.
 */
@Embeddable
public class PreferenciaColumnasId implements Serializable {

    @Column(name = "username", length = 128, nullable = false)
    private String username;

    @Column(name = "reporte", length = 32, nullable = false)
    private String reporte;

    public PreferenciaColumnasId() {
    }

    public PreferenciaColumnasId(String username, String reporte) {
        this.username = username;
        this.reporte = reporte;
    }

    public String getUsername() {
        return username;
    }

    public PreferenciaColumnasId setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getReporte() {
        return reporte;
    }

    public PreferenciaColumnasId setReporte(String reporte) {
        this.reporte = reporte;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PreferenciaColumnasId that = (PreferenciaColumnasId) o;
        return new EqualsBuilder()
                .append(username, that.username)
                .append(reporte, that.reporte)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(username)
                .append(reporte)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("username", username)
                .append("reporte", reporte)
                .toString();
    }
}

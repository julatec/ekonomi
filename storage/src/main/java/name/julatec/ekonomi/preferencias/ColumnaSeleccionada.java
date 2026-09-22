package name.julatec.ekonomi.preferencias;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Una columna del reporte dentro de la preferencia de alguien: cómo se llama y si se ve.
 * <p>
 * Guarda también las <b>ocultas</b>, y eso es el punto: si solo se guardaran las visibles, una
 * columna apagada perdería su lugar y al volver a prenderla reaparecería al final. Guardando
 * las dos cosas, el orden es uno solo —el de la lista— y la visibilidad es una marca encima.
 * <p>
 * El nombre es el encabezado tal cual sale en el {@code .xlsx} (el de
 * {@code @CsvBindByName}), no un identificador aparte. Es lo que la persona ve en la hoja y lo
 * que {@code @CsvBindByNameOrder} ya usa para ordenar, así que inventar una segunda clave
 * abriría la puerta a que las dos dejaran de coincidir. Lo que cuesta es que renombrar una
 * columna huérfana la preferencia: {@link ColumnasDeReporte#reconciliar} descarta lo que no
 * esté en el catálogo de hoy en vez de escribir una columna que ya no existe.
 */
@Embeddable
public class ColumnaSeleccionada implements Serializable {

    @Column(name = "columna", length = 64, nullable = false)
    private String columna;

    @Column(name = "visible", nullable = false)
    private boolean visible = true;

    public ColumnaSeleccionada() {
    }

    public ColumnaSeleccionada(String columna, boolean visible) {
        this.columna = columna;
        this.visible = visible;
    }

    public String getColumna() {
        return columna;
    }

    public ColumnaSeleccionada setColumna(String columna) {
        this.columna = columna;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public ColumnaSeleccionada setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ColumnaSeleccionada that = (ColumnaSeleccionada) o;
        return new EqualsBuilder()
                .append(visible, that.visible)
                .append(columna, that.columna)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(columna)
                .append(visible)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("columna", columna)
                .append("visible", visible)
                .toString();
    }
}

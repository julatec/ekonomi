package name.julatec.ekonomi.tribunet.storage;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Emisor {

    @Column(name = "emisor_nombre")
    protected String nombre;

    @Column(name = "emisor_tipo")
    protected String tipo;

    @Column(name = "emisor_numero")
    protected String numero;

    public String getNombre() {
        return nombre;
    }

    public Emisor setNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public String getTipo() {
        return tipo;
    }

    public Emisor setTipo(String tipo) {
        this.tipo = tipo;
        return this;
    }

    public String getNumero() {
        return numero;
    }

    public Emisor setNumero(String numero) {
        this.numero = numero;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Emisor emisor = (Emisor) o;

        return new EqualsBuilder()
                .append(nombre, emisor.nombre)
                .append(tipo, emisor.tipo)
                .append(numero, emisor.numero)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(nombre)
                .append(tipo)
                .append(numero)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("nombre", nombre)
                .append("tipo", tipo)
                .append("numero", numero)
                .toString();
    }

}

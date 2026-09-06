package name.julatec.ekonomi.actividad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Una fila de la correspondencia ATV (CIIU 3, el código de actividad de Hacienda) → TRIBU-CR
 * (CIIU 4), publicada por Hacienda como PDF/XLSX.
 * <p>
 * {@code atv} es el mismo espacio de códigos que {@code Documento.codigoActividadEmisor}/
 * {@code codigoActividadReceptor} — de ahí que valga la pena tenerla acá: hoy esos dos campos
 * se muestran como un número pelado en la UI, sin decir a qué actividad corresponde.
 * <p>
 * Vive en la base «primaria» (igual que {@code CabysItem}), no por tenant: es un catálogo que
 * publica Hacienda y es idéntico para todas las contabilidades. A diferencia de CABYS, no está
 * versionado — la fuente es una extracción manual de un PDF de octubre 2025 sin un esquema de
 * versiones declarado por Hacienda, así que no hay «vigente en esta fecha» que calcular.
 * <p>
 * <b>Dato conocido y no corregido a propósito:</b> de las 751 filas de la fuente, 696 traen
 * {@code atv} de 6 dígitos (el mismo largo que exige un comprobante real) y 51 de 5 — muy
 * probablemente un cero inicial perdido al extraer el PDF, pero sin evidencia de cuál falta en
 * cada caso. Una búsqueda exacta por el código de 6 dígitos de un comprobante no va a encontrar
 * esas 51 filas. No se les agrega un cero a ciegas.
 *
 * @see ActividadEconomicaRepository
 */
@Entity(name = "actividadEconomica")
@Table(name = "actividad_economica", indexes = {
        @Index(name = "actividad_economica_atv_idx", columnList = "atv"),
})
public class ActividadEconomica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código ATV (CIIU 3) de Hacienda — el que trae el comprobante. {@code "N/A"} si la subclase CIIU4 no tiene predecesor. */
    @Column(length = 6, nullable = false)
    private String atv;

    @Column(name = "atv_nombre", length = 400, nullable = false)
    private String atvNombre;

    /** Subclase TRIBU-CR, formato {@code NNNN.N}. */
    @Column(length = 6, nullable = false)
    private String ciiu4;

    /** Los cuatro dígitos de {@link #ciiu4}, para unir con la división que usan los libros contables. */
    @Column(length = 4, nullable = false)
    private String division;

    @Column(name = "ciiu4_nombre", length = 400, nullable = false)
    private String ciiu4Nombre;

    @Column(length = 400)
    private String especialidad;

    public Long getId() {
        return id;
    }

    public ActividadEconomica setId(Long id) {
        this.id = id;
        return this;
    }

    public String getAtv() {
        return atv;
    }

    public ActividadEconomica setAtv(String atv) {
        this.atv = atv;
        return this;
    }

    public String getAtvNombre() {
        return atvNombre;
    }

    public ActividadEconomica setAtvNombre(String atvNombre) {
        this.atvNombre = atvNombre;
        return this;
    }

    public String getCiiu4() {
        return ciiu4;
    }

    public ActividadEconomica setCiiu4(String ciiu4) {
        this.ciiu4 = ciiu4;
        return this;
    }

    public String getDivision() {
        return division;
    }

    public ActividadEconomica setDivision(String division) {
        this.division = division;
        return this;
    }

    public String getCiiu4Nombre() {
        return ciiu4Nombre;
    }

    public ActividadEconomica setCiiu4Nombre(String ciiu4Nombre) {
        this.ciiu4Nombre = ciiu4Nombre;
        return this;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public ActividadEconomica setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
        return this;
    }
}

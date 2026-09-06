package name.julatec.ekonomi.cabys;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Un código del Catálogo de Bienes y Servicios de Hacienda/BCCR, en una versión concreta.
 * <p>
 * Vive en la base «primaria» (`jdbc/ekonomiPrimary`, hoy `julatec_ekonomi`), no en la de
 * ningún tenant. Es la misma base donde ya viven {@code User} e {@code Issuer}: lo que las
 * tres tienen en común es no pertenecer a ninguna contabilidad — CABYS lo publica Hacienda y
 * es idéntico para todos, así que duplicarlo por tenant abriría la puerta a que dos
 * contabilidades terminaran validando contra versiones distintas del mismo estándar nacional,
 * un modo de fallar silencioso. Decisión registrada en
 * {@code 04-proyectos/ekonomi/cabys-diseno.md} del repo de operaciones (1 sep 2026).
 * <p>
 * Es de <b>solo lectura desde la aplicación</b>: no hay {@code save()} expuesto a ningún
 * controlador ni herramienta. La tabla se carga con un script aparte —igual que las
 * conversiones de charset— porque el catálogo cambia cada par de años; un cron para eso
 * sería infraestructura que se rompe sola la mayor parte del tiempo sin hacer nada.
 *
 * @see CabysVersion
 */
@Entity(name = "cabysItem")
@Table(name = "cabys_item", indexes = {
        @Index(name = "cabys_item_descripcion_idx", columnList = "descripcion"),
})
public class CabysItem {

    @EmbeddedId
    private CabysItemId id;

    @Column(length = 500, nullable = false)
    private String descripcion;

    /** La categoría de más alto nivel (dígito 1), para agrupar sin tener que decodificar. */
    @Column(length = 200)
    private String categoria1;

    /**
     * La tarifa de IVA que CABYS dicta para este código, como fracción ({@code 0.13} = 13 %).
     * <p>
     * Nula cuando el catálogo dice {@code n/a} — existen unos pocos códigos así, no es un
     * error de carga. Ver {@link #exento} para la otra forma de "sin tarifa positiva".
     */
    @Column(precision = 4, scale = 2)
    private BigDecimal tarifa;

    /** El catálogo lo marca "Exento" en vez de una tarifa: no es lo mismo que tarifa 0 %. */
    @Column(nullable = false)
    private boolean exento;

    public CabysItemId getId() {
        return id;
    }

    public CabysItem setId(CabysItemId id) {
        this.id = id;
        return this;
    }

    public String getCodigo() {
        return id == null ? null : id.getCodigo();
    }

    public String getVersion() {
        return id == null ? null : id.getVersion();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public CabysItem setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public String getCategoria1() {
        return categoria1;
    }

    public CabysItem setCategoria1(String categoria1) {
        this.categoria1 = categoria1;
        return this;
    }

    public BigDecimal getTarifa() {
        return tarifa;
    }

    public CabysItem setTarifa(BigDecimal tarifa) {
        this.tarifa = tarifa;
        return this;
    }

    public boolean isExento() {
        return exento;
    }

    public CabysItem setExento(boolean exento) {
        this.exento = exento;
        return this;
    }
}

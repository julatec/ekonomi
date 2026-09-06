package name.julatec.ekonomi.cabys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Una versión cargada del catálogo CABYS.
 * <p>
 * Existe para poder decir «este comprobante se validó contra el catálogo vigente en su
 * fecha», no solo contra el que esté cargado hoy — requisito explícito de la resolución del
 * 28 ago 2026 sobre {@code cabys-diseno.md}. Hacienda/BCCR no publican el catálogo con
 * ediciones numeradas y una fecha de vigencia única y prolija: es una lista que se enmienda
 * código por código. {@link #vigenteDesde} es la mejor aproximación disponible al cargar cada
 * versión —normalmente la fecha del archivo fuente— y no una fecha oficial de publicación.
 */
@Entity(name = "cabysVersion")
@Table(name = "cabys_version")
public class CabysVersion {

    @Id
    @Column(length = 20)
    private String version;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    @Column(name = "cargado_en", nullable = false)
    private LocalDateTime cargadoEn;

    /** De dónde salió el archivo cargado, para poder rastrear una descripción rara hasta su origen. */
    @Column(length = 300)
    private String fuente;

    public String getVersion() {
        return version;
    }

    public CabysVersion setVersion(String version) {
        this.version = version;
        return this;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public CabysVersion setVigenteDesde(LocalDate vigenteDesde) {
        this.vigenteDesde = vigenteDesde;
        return this;
    }

    public LocalDateTime getCargadoEn() {
        return cargadoEn;
    }

    public CabysVersion setCargadoEn(LocalDateTime cargadoEn) {
        this.cargadoEn = cargadoEn;
        return this;
    }

    public String getFuente() {
        return fuente;
    }

    public CabysVersion setFuente(String fuente) {
        this.fuente = fuente;
        return this;
    }
}

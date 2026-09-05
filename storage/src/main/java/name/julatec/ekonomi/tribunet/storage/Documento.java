package name.julatec.ekonomi.tribunet.storage;

import org.apache.commons.lang3.builder.ToStringBuilder;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Lob;
import java.util.Date;

@Embeddable
public class Documento {

    @Column(name = "fecha_emision")
    private Date fechaEmision;

    @Embedded
    private Emisor emisor;

    @Embedded
    private Receptor receptor;

    @Embedded
    private Resumen resumen;

    /**
     * El comprobante completo, tal como llegó. Son decenas de kilobytes: uno real medido
     * pesa 29 KB.
     * <p>
     * El {@code length} explícito no es adorno. Sin él, JPA deja el default en 255 y el
     * {@code MySQLDialect} de Hibernate 7 elige el tipo por escalera de tamaño: la columna
     * sale {@code tinytext}, y con {@code STRICT_TRANS_TABLES} el INSERT de un XML real
     * falla con el error 1406 en vez de truncar. Contra un esquema vacío —el ambiente
     * local— eso es exactamente lo que pasaba.
     * <p>
     * Y al revés: Hibernate 7 sí sabe angostar columnas, cosa que Hibernate 5 no hacía. Con
     * la anotación sin {@code length}, apuntar la aplicación a la base real con
     * {@code hbm2ddl.auto=update} emitiría un {@code alter ... modify column document
     * tinytext} sobre la contabilidad. Hoy no ocurre porque el valor por omisión es
     * {@code none} desde mayo de 2025 —16 meses antes de que entrara Hibernate 7—, pero eso
     * es una propiedad, no una barrera.
     */
    @Lob
    @Column(length = Integer.MAX_VALUE)
    private String document;

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public Documento setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
        return this;
    }

    public Emisor getEmisor() {
        return emisor;
    }

    public Documento setEmisor(Emisor emisor) {
        this.emisor = emisor;
        return this;
    }

    public Receptor getReceptor() {
        return receptor;
    }

    public Documento setReceptor(Receptor receptor) {
        this.receptor = receptor;
        return this;
    }

    public Resumen getResumen() {
        return resumen;
    }

    public Documento setResumen(Resumen resumen) {
        this.resumen = resumen;
        return this;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("fechaEmision", fechaEmision)
                .append("emisor", emisor)
                .append("receptor", receptor)
                .toString();
    }

    public String getDocument() {
        return document;
    }

    public Documento setDocument(String document) {
        this.document = document;
        return this;
    }

}

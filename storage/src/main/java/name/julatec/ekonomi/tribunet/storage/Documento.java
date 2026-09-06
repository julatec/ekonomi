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

    /**
     * Código de actividad económica del emisor, de 6 dígitos.
     * <p>
     * Existe desde v4.3 de Hacienda (v4.2 no lo traía). En v4.3 era un único campo sin
     * distinguir de quién; v4.4 lo partió en emisor/receptor, y acá guarda siempre el del
     * emisor — {@code Documento.getCodigoActividadEmisorEfectivo()} en el adaptador ya
     * resuelve esa diferencia de versión antes de llegar a esta columna.
     * <p>
     * No hay {@code columnDefinition}: por lo mismo que el charset de {@link #document} no
     * se declara acá, un {@code VARCHAR(6)} le basta a Hibernate para el DDL, y no hace
     * falta nada más específico de un motor.
     */
    @Column(name = "codigo_actividad_emisor", length = 6)
    private String codigoActividadEmisor;

    /**
     * Código de actividad económica del receptor, cuando el documento lo trae.
     * <p>
     * Solo existe en v4.4, y ahí casi siempre es opcional — la excepción es la factura de
     * compra, donde es obligatorio porque el receptor es quien declara el gasto.
     */
    @Column(name = "codigo_actividad_receptor", length = 6)
    private String codigoActividadReceptor;

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
    /*
     * 🔴 EL JUEGO DE CARACTERES NO SE DECLARA ACA, Y ESO ES DELIBERADO.
     *
     * Las columnas de produccion eran `latin1`, y eso hacia que un comprobante con un
     * caracter fuera de ese juego NO SE GUARDARA. Medido el 5 sep 2026: 840 fallos en un dia
     * sobre `julatec_tribuconta.factura`, todos con
     *
     *     Incorrect string value: '\xE2\x82\xA1…'   (U+20A1, el simbolo del colon)
     *
     * El emisor que escribe `¢` (0xA2, existe en latin1) pasaba; el que escribe `₡` reventaba
     * y el documento se perdia en silencio — llegaba por correo y no quedaba en ningun lado.
     *
     * La correccion NO va en una `columnDefinition` de esta anotacion: eso solo lo usa la
     * generacion de DDL, es especifico de MySQL y romperia cualquier otro motor. Va en el
     * JUEGO POR OMISION DE LA TABLA, que es lo que Hibernate hereda cuando emite un
     * `modify column ... longtext` sin charset. Con la tabla en utf8mb4, un eventual
     * `hbm2ddl.auto=update` conserva el juego en vez de revertirlo a latin1.
     *
     * Estado al 6 sep 2026: CONVERSION COMPLETA. Las columnas de texto largo de los dos
     * esquemas (`document` en factura/factura_compra/factura_exportacion/nota_credito/
     * nota_debito, y `mensaje_hacienda`/`mensaje_receptor` en `mensaje`) estan en utf8mb4
     * en las dos bases. La de `julatec_tribuconta.factura` (751 MB, 55.991 filas) tardo 5
     * segundos: `ALTER TABLE ... MODIFY ... CHARACTER SET utf8mb4` no reescribe el dato
     * caracter por caracter — solo cambia la interpretacion de los mismos bytes, porque
     * todo byte que YA estaba en la columna es, por construccion, valido en latin1: un
     * caracter fuera de ese juego jamas se pudo insertar, que es justo el bug que esto
     * corrige. No hubo que tocar una fila.
     *
     * Antes de convertir se buscaron indicios de doble codificacion (texto UTF-8 guardado
     * crudo en una columna latin1, que una `ALTER ... CHARACTER SET` preserva tal cual en
     * vez de arreglar). Se encontraron 6 en `julatec_tribuconta.factura` — una Ñ guardada
     * como dos caracteres latin1 (`Ã±`) en vez de uno — de una importacion previa que no
     * paso por este codigo. No las corrompio esta conversion: ya estaban asi. Quedan para
     * revisarlas aparte; las claves estan en el commit que aplico esto.
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

    public String getCodigoActividadEmisor() {
        return codigoActividadEmisor;
    }

    public Documento setCodigoActividadEmisor(String codigoActividadEmisor) {
        this.codigoActividadEmisor = codigoActividadEmisor;
        return this;
    }

    public String getCodigoActividadReceptor() {
        return codigoActividadReceptor;
    }

    public Documento setCodigoActividadReceptor(String codigoActividadReceptor) {
        this.codigoActividadReceptor = codigoActividadReceptor;
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

package name.julatec.ekonomi.preferencias;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Qué columnas quiere ver alguien en un reporte, y en qué orden.
 * <p>
 * Vive en la base «primaria» ({@code jdbc/ekonomiPrimary}, hoy {@code julatec_ekonomi}), la
 * misma de {@code User} e {@code Issuer} y por el mismo motivo que CABYS: no pertenece a
 * ninguna contabilidad. La preferencia es de la <b>persona</b>, no del tenant — quien abre
 * julatec y tribuconta el mismo día no quiere reacomodar las columnas al cambiar de libro.
 * <p>
 * <b>Sin llave foránea contra {@code user}</b>, a propósito: una fila huérfana acá no rompe
 * nada (se ignora al leer) y no vale la pena que borrar un usuario dependa de esta tabla.
 * <p>
 * <b>Hace falta escribir en la base primaria.</b> Es la primera cosa de la aplicación que lo
 * necesita en operación normal —hasta hoy solo escribían las siembras, y la primera corrida de
 * {@code SembradorKilla} falló justamente porque la base estaba en {@code --read-only}—. Sin
 * permiso de escritura la aplicación sigue funcionando: los reportes salen con el orden por
 * omisión y guardar responde 503 con el mensaje de la base, en vez de fallar callado.
 * <p>
 * Contra la base real {@code ddl-auto} es {@code none} (ver {@code SecurityConfig}), así que
 * las dos tablas se crean a mano. Esto es lo que Hibernate generó en el ambiente local,
 * copiado del {@code SHOW CREATE TABLE} —no de lo que uno supone que genera— el 22 set 2026:
 * <pre>
 * CREATE TABLE preferencia_columnas (
 *   reporte  VARCHAR(32)  NOT NULL,
 *   username VARCHAR(128) NOT NULL,
 *   PRIMARY KEY (reporte, username)
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
 *
 * CREATE TABLE preferencia_columna (
 *   reporte  VARCHAR(32)  NOT NULL,
 *   username VARCHAR(128) NOT NULL,
 *   columna  VARCHAR(64)  NOT NULL,
 *   visible  BIT(1)       NOT NULL,
 *   posicion INT          NOT NULL,
 *   PRIMARY KEY (reporte, username, posicion),
 *   CONSTRAINT preferencia_columna_fk FOREIGN KEY (reporte, username)
 *     REFERENCES preferencia_columnas (reporte, username),
 *   CONSTRAINT preferencia_columna_chk_1 CHECK (posicion >= 0)
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
 * </pre>
 * El orden de las columnas de la llave es {@code (reporte, username)} y no al revés porque así
 * lo emite Hibernate; el nombre de la foránea lo inventa él ({@code FKc6kfq...}) y en un DDL a
 * mano da igual. El juego de caracteres es el del contenedor local: contra la base real
 * conviene igualarlo al de las tablas que ya están ahí.
 */
@Entity(name = "preferenciaColumnas")
@Table(name = "preferencia_columnas")
public class PreferenciaColumnas {

    @EmbeddedId
    private PreferenciaColumnasId id;

    /**
     * Todas las columnas que la persona ordenó, visibles y ocultas, en su orden.
     * <p>
     * {@code EAGER} porque nunca se lee la fila sin querer la lista: es lo único que tiene.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "preferencia_columna",
            joinColumns = {
                    @JoinColumn(name = "username", referencedColumnName = "username"),
                    @JoinColumn(name = "reporte", referencedColumnName = "reporte")
            })
    @OrderColumn(name = "posicion")
    private List<ColumnaSeleccionada> columnas = new ArrayList<>();

    public PreferenciaColumnas() {
    }

    public PreferenciaColumnas(PreferenciaColumnasId id, List<ColumnaSeleccionada> columnas) {
        this.id = id;
        this.columnas = new ArrayList<>(columnas);
    }

    public PreferenciaColumnasId getId() {
        return id;
    }

    public PreferenciaColumnas setId(PreferenciaColumnasId id) {
        this.id = id;
        return this;
    }

    public List<ColumnaSeleccionada> getColumnas() {
        return columnas;
    }

    public PreferenciaColumnas setColumnas(List<ColumnaSeleccionada> columnas) {
        // La misma lista, no otra: Hibernate rastrea la instancia que le entregó.
        this.columnas.clear();
        this.columnas.addAll(columnas);
        return this;
    }
}

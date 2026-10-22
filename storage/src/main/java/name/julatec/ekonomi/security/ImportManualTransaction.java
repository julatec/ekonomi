package name.julatec.ekonomi.security;

import jakarta.persistence.Entity;

/**
 * Cuenta de importación de comprobantes manuales. <b>Retirada el 22 oct 2026:</b> la subida de
 * comprobantes manuales (el CSV que se escribía en {@code manual_transaction}) se quitó, y esta
 * clase ya no la lee ni la escribe nadie.
 * <p>
 * Se deja a propósito, solo como mapeo de lo que ya está guardado: es la tabla
 * {@code import_manual_transaction} y, por {@link User#getImportManualTransactions()}, la relación
 * {@code user_import_manual_account} de los usuarios, ambas de la base primaria. Quitarla no toca
 * el esquema ({@code ddl-auto} es {@code none} contra la base real) pero deja filas huérfanas y
 * cambia la forma de {@link User}, que viaja serializado en la sesión HTTP; eso es una decisión
 * aparte, que pide mirar primero qué hay en esas tablas.
 */
@Entity(name = "import_manual_transaction")
public class ImportManualTransaction extends ImportTransaction<ImportManualTransaction> {

}

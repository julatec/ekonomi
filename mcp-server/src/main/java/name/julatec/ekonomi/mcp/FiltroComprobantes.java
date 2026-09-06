package name.julatec.ekonomi.mcp;

import name.julatec.ekonomi.tribunet.storage.ComprobanteSpecs;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Los criterios de búsqueda, ya validados y convertidos.
 * <p>
 * Todos son opcionales y se combinan con Y lógico. Ninguno solo es obligatorio,
 * pero una búsqueda sin ningún criterio devuelve el tope de filas y poco más,
 * así que las herramientas exigen al menos uno.
 */
public record FiltroComprobantes(
        String clave,
        String consecutivo,
        String cedula,
        String nombre,
        /**
         * Una parte concreta del documento, por identificación exacta o por nombre.
         * <p>
         * {@code cedula} y {@code nombre} buscan en los dos lados a la vez, que es lo correcto
         * cuando se busca «todo lo de esta contraparte». Estos dos sirven para la pregunta
         * distinta —«lo que ME facturó fulano» contra «lo que YO le facturé»— que antes obligaba
         * a mirar la columna a ojo.
         */
        String emisor,
        String receptor,
        Date desde,
        Date hasta,
        BigDecimal montoMinimo,
        BigDecimal montoMaximo,
        String moneda,
        /**
         * Código de actividad económica de 6 dígitos, de cualquiera de las dos partes.
         * <p>
         * Existe desde v4.3 de Hacienda; los comprobantes de v4.2 (2016/2017) siempre dan
         * {@code null} acá, así que este filtro nunca los va a traer. Es una limitación del
         * formato, no de la búsqueda.
         */
        String codigoActividad) {

    public boolean vacio() {
        return clave == null && consecutivo == null && cedula == null && nombre == null
                && emisor == null && receptor == null
                && desde == null && hasta == null
                && montoMinimo == null && montoMaximo == null && moneda == null
                && codigoActividad == null;
    }

    public static Date fecha(String nombre, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Date.from(LocalDate.parse(valor.trim())
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ir como AAAA-MM-DD; llegó: " + valor);
        }
    }

    /**
     * La fecha final se lleva al último instante del día: quien pide
     * {@code hasta=2026-03-31} espera que las facturas de ese 31 entren.
     */
    public static Date fechaFinal(String nombre, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Date.from(LocalDate.parse(valor.trim())
                    .plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ir como AAAA-MM-DD; llegó: " + valor);
        }
    }

    public static BigDecimal monto(String nombre, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un número sin separador de miles; llegó: " + valor);
        }
    }

    private static String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static final java.util.regex.Pattern CODIGO_ACTIVIDAD = java.util.regex.Pattern.compile("\\d{6}");

    public static FiltroComprobantes de(
            String clave, String consecutivo, String cedula, String nombre,
            String emisor, String receptor,
            String desde, String hasta, String montoMinimo, String montoMaximo, String moneda,
            String codigoActividad) {
        final BigDecimal minimo = monto("monto_minimo", montoMinimo);
        final BigDecimal maximo = monto("monto_maximo", montoMaximo);
        if (minimo != null && maximo != null && minimo.compareTo(maximo) > 0) {
            throw new IllegalArgumentException(
                    "monto_minimo (" + minimo + ") es mayor que monto_maximo (" + maximo + ").");
        }
        final Date inicio = fecha("desde", desde);
        final Date fin = fechaFinal("hasta", hasta);
        if (inicio != null && fin != null && inicio.after(fin)) {
            throw new IllegalArgumentException("La fecha `desde` es posterior a `hasta`.");
        }
        final String actividad = limpio(codigoActividad);
        if (actividad != null && !CODIGO_ACTIVIDAD.matcher(actividad).matches()) {
            throw new IllegalArgumentException(
                    "El código de actividad son 6 dígitos exactos; llegó: " + actividad);
        }
        return new FiltroComprobantes(
                limpio(clave), limpio(consecutivo), limpio(cedula), limpio(nombre),
                limpio(emisor), limpio(receptor),
                inicio, fin, minimo, maximo, limpio(moneda), actividad);
    }

    /** Traduce los criterios a un filtro aplicable a cualquiera de los cinco tipos. */
    public <T> Specification<T> aSpecification() {
        return ComprobanteSpecs.todos(
                // Una clave completa son 50 dígitos y es la llave primaria: por igualdad usa el
                // índice, mientras que `like %…%` obliga a barrer las cinco tablas. El fragmento
                // sigue existiendo para quien pegue un pedazo.
                clave == null ? null
                        : clave.length() == 50 && clave.chars().allMatch(Character::isDigit)
                        ? ComprobanteSpecs.<T>clave(clave)
                        : ComprobanteSpecs.<T>claveContiene(clave),
                consecutivo == null ? null : ComprobanteSpecs.<T>consecutivo(consecutivo),
                cedula == null ? null : ComprobanteSpecs.<T>parteNumero(cedula),
                nombre == null ? null : ComprobanteSpecs.<T>nombreContiene(nombre),
                emisor == null ? null : ComprobanteSpecs.<T>emisorEs(emisor),
                receptor == null ? null : ComprobanteSpecs.<T>receptorEs(receptor),
                desde == null ? null : ComprobanteSpecs.<T>desde(desde),
                hasta == null ? null : ComprobanteSpecs.<T>hasta(hasta),
                montoMinimo == null ? null : ComprobanteSpecs.<T>montoDesde(montoMinimo),
                montoMaximo == null ? null : ComprobanteSpecs.<T>montoHasta(montoMaximo),
                moneda == null ? null : ComprobanteSpecs.<T>moneda(moneda),
                codigoActividad == null ? null : ComprobanteSpecs.<T>codigoActividad(codigoActividad));
    }
}

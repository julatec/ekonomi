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
        Date desde,
        Date hasta,
        BigDecimal montoMinimo,
        BigDecimal montoMaximo,
        String moneda) {

    public boolean vacio() {
        return clave == null && consecutivo == null && cedula == null && nombre == null
                && desde == null && hasta == null
                && montoMinimo == null && montoMaximo == null && moneda == null;
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

    public static FiltroComprobantes de(
            String clave, String consecutivo, String cedula, String nombre,
            String desde, String hasta, String montoMinimo, String montoMaximo, String moneda) {
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
        return new FiltroComprobantes(
                limpio(clave), limpio(consecutivo), limpio(cedula), limpio(nombre),
                inicio, fin, minimo, maximo, limpio(moneda));
    }

    /** Traduce los criterios a un filtro aplicable a cualquiera de los cinco tipos. */
    public <T> Specification<T> aSpecification() {
        return ComprobanteSpecs.todos(
                clave == null ? null : ComprobanteSpecs.<T>claveContiene(clave),
                consecutivo == null ? null : ComprobanteSpecs.<T>consecutivo(consecutivo),
                cedula == null ? null : ComprobanteSpecs.<T>parteNumero(cedula),
                nombre == null ? null : ComprobanteSpecs.<T>nombreContiene(nombre),
                desde == null ? null : ComprobanteSpecs.<T>desde(desde),
                hasta == null ? null : ComprobanteSpecs.<T>hasta(hasta),
                montoMinimo == null ? null : ComprobanteSpecs.<T>montoDesde(montoMinimo),
                montoMaximo == null ? null : ComprobanteSpecs.<T>montoHasta(montoMaximo),
                moneda == null ? null : ComprobanteSpecs.<T>moneda(moneda));
    }
}

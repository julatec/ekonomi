package name.julatec.ekonomi.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Un comprobante completo, listo para dibujar.
 * <p>
 * Es un DTO propio y no las interfaces de {@code tribunet-adapters} serializadas: aquéllas
 * devuelven {@code Stream<>} de un solo uso, exponen alias deprecados de {@code FactorIVA} y
 * son proxies generados por un annotation processor. Nada de eso es un contrato estable para
 * un frontend.
 * <p>
 * Las etiquetas de tarifa se resuelven acá, en Java, con {@code FactorIVA}, que ya tiene el
 * código oficial de Hacienda y su descripción. La interfaz no debería tener que conocer la
 * Nota 8.1.
 */
public record DetalleComprobante(
        String tenant,
        String tipo,
        String clave,
        String consecutivo,
        String fechaEmision,
        Parte emisor,
        Parte receptor,
        Resumen resumen,
        List<Linea> lineas,
        List<Referencia> referencias,
        String xml) {

    public record Parte(String nombre, String tipoIdentificacion, String numero) {
    }

    public record Resumen(
            String moneda,
            BigDecimal tipoCambio,
            BigDecimal totalGravado,
            BigDecimal totalExento,
            BigDecimal totalVenta,
            BigDecimal totalDescuentos,
            BigDecimal totalVentaNeta,
            BigDecimal totalImpuesto,
            BigDecimal totalOtrosCargos,
            BigDecimal totalComprobante) {
    }

    public record Linea(
            BigInteger numeroLinea,
            String detalle,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            BigDecimal montoTotal,
            BigDecimal subTotal,
            BigDecimal impuestoNeto,
            BigDecimal montoTotalLinea,
            List<Impuesto> impuestos) {
    }

    public record Impuesto(
            String codigo,
            String codigoTarifa,
            String descripcionTarifa,
            BigDecimal tarifaPorcentaje,
            BigDecimal monto,
            Exoneracion exoneracion) {
    }

    public record Exoneracion(
            String tipoDocumento,
            String numeroDocumento,
            String nombreInstitucion,
            BigInteger porcentaje,
            BigDecimal monto) {
    }

    /**
     * Solo las notas de crédito y débito la traen con sentido: es el documento que ajustan o
     * anulan, y sin ella el comprobante no se entiende. En los otros tres tipos el nodo existe
     * en el esquema pero suele venir vacío.
     * <p>
     * Los dos códigos viajan con su descripción resuelta al lado, y no traducidos en el
     * lugar: las Notas 9 y 10 mandan mostrar la descripción, pero el código es lo que trae el
     * documento firmado y lo que hay que poder citar si algo no cuadra.
     */
    public record Referencia(
            String tipoDoc,
            String descripcionTipoDoc,
            String numero,
            String fechaEmision,
            String codigo,
            String descripcionCodigo,
            String razon) {
    }
}

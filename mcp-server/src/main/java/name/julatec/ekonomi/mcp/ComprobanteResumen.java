package name.julatec.ekonomi.mcp;

import name.julatec.ekonomi.tribunet.storage.ElectronicReceipt;
import name.julatec.ekonomi.tribunet.storage.Resumen;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

/**
 * Una fila de resultado, plana y sin el XML.
 * <p>
 * El {@code documento} guardado incluye el comprobante completo, que son decenas
 * de kilobytes por factura. Devolverlo en una búsqueda llenaría la ventana de
 * contexto sin aportar: para el detalle está {@code detalle_comprobante}.
 */
public record ComprobanteResumen(
        String tipo,
        String clave,
        String consecutivo,
        String fechaEmision,
        String emisorNumero,
        String emisorNombre,
        String receptorNumero,
        String receptorNombre,
        String moneda,
        BigDecimal tipoCambio,
        BigDecimal totalGravado,
        BigDecimal totalExento,
        BigDecimal totalImpuesto,
        BigDecimal totalComprobante) {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private static String fecha(Date date) {
        return date == null ? null : ISO.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public static ComprobanteResumen de(String tipo, ElectronicReceipt receipt) {
        final Resumen resumen = Optional.ofNullable(receipt.getDocumento())
                .map(d -> d.getResumen())
                .orElse(null);
        return new ComprobanteResumen(
                tipo,
                receipt.getClave(),
                receipt.getRecordSequential(),
                fecha(Optional.ofNullable(receipt.getDocumento())
                        .map(d -> d.getFechaEmision()).orElse(null)),
                receipt.getEmisor().map(e -> e.getNumero()).orElse(null),
                receipt.getEmisor().map(e -> e.getNombre()).orElse(null),
                receipt.getReceptor().map(r -> r.getNumero()).orElse(null),
                receipt.getReceptor().map(r -> r.getNombre()).orElse(null),
                resumen == null ? null : resumen.getCodigoMoneda(),
                resumen == null ? null : resumen.getTipoCambio(),
                resumen == null ? null : resumen.getTotalGravado(),
                resumen == null ? null : resumen.getTotalExento(),
                resumen == null ? null : resumen.getTotalImpuesto(),
                resumen == null ? null : resumen.getTotalComprobante());
    }
}

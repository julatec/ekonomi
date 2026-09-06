package name.julatec.ekonomi.mcp;

import name.julatec.ekonomi.tribunet.DetailedDocument;
import name.julatec.ekonomi.tribunet.FactorIVA;
import name.julatec.ekonomi.tribunet.storage.ElectronicReceipt;
import name.julatec.ekonomi.tribunet.storage.Resumen;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
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
        /**
         * Código de actividad económica de 6 dígitos, del emisor y del receptor por
         * separado. Solo existe desde v4.3 de Hacienda; en un comprobante de v4.2
         * (2016/2017) los dos vienen {@code null}.
         */
        String codigoActividadEmisor,
        String codigoActividadReceptor,
        String moneda,
        BigDecimal tipoCambio,
        BigDecimal totalGravado,
        BigDecimal totalExento,
        BigDecimal totalImpuesto,
        BigDecimal totalComprobante,
        /**
         * El desglose por tarifa, igual que las columnas del Excel ({@code Voucher}). Viene
         * vacío en la fila recién armada por {@link #de}: exige reparsear el XML completo, así
         * que {@code BusquedaComprobantes} lo llena aparte con {@link #conImpuestos}, y solo
         * para las filas que de verdad se van a devolver — no para las que el límite descarta
         * al mezclar los cinco tipos de comprobante.
         */
        List<TasaImpuesto> impuestosPorTarifa) {

    /** Una fila del desglose: la tarifa, y cuánto de esta factura cayó en ella. */
    public record TasaImpuesto(String codigo, String etiqueta, BigDecimal baseImponible, BigDecimal impuesto) {
    }

    /**
     * Las 11 tarifas de Hacienda, en el mismo orden que las columnas del Excel — que no es el
     * orden numérico de los códigos ({@code T01, T09, T02...}), sino el que ya tenía
     * {@code Voucher} desde antes.
     */
    private static final List<TarifaConEtiqueta> TARIFAS = List.of(
            new TarifaConEtiqueta(FactorIVA.T01, "0% Art.32"),
            new TarifaConEtiqueta(FactorIVA.T09, "0.5%"),
            new TarifaConEtiqueta(FactorIVA.T02, "1%"),
            new TarifaConEtiqueta(FactorIVA.T03, "2%"),
            new TarifaConEtiqueta(FactorIVA.T04, "4%"),
            new TarifaConEtiqueta(FactorIVA.T05, "Transitorio 0%"),
            new TarifaConEtiqueta(FactorIVA.T06, "Transitorio 4%"),
            new TarifaConEtiqueta(FactorIVA.T07, "8%"),
            new TarifaConEtiqueta(FactorIVA.T08, "13%"),
            new TarifaConEtiqueta(FactorIVA.T10, "Exenta"),
            new TarifaConEtiqueta(FactorIVA.T11, "0% sin crédito"));

    private record TarifaConEtiqueta(FactorIVA tarifa, String etiqueta) {
    }

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
                Optional.ofNullable(receipt.getDocumento())
                        .map(d -> d.getCodigoActividadEmisor()).orElse(null),
                Optional.ofNullable(receipt.getDocumento())
                        .map(d -> d.getCodigoActividadReceptor()).orElse(null),
                resumen == null ? null : resumen.getCodigoMoneda(),
                resumen == null ? null : resumen.getTipoCambio(),
                resumen == null ? null : resumen.getTotalGravado(),
                resumen == null ? null : resumen.getTotalExento(),
                resumen == null ? null : resumen.getTotalImpuesto(),
                resumen == null ? null : resumen.getTotalComprobante(),
                List.of());
    }

    /** Nueva instancia con el desglose por tarifa ya calculado; el resto de los campos igual. */
    public ComprobanteResumen conImpuestos(List<TasaImpuesto> impuestosPorTarifa) {
        return new ComprobanteResumen(tipo, clave, consecutivo, fechaEmision, emisorNumero, emisorNombre,
                receptorNumero, receptorNombre, codigoActividadEmisor, codigoActividadReceptor, moneda,
                tipoCambio, totalGravado, totalExento, totalImpuesto, totalComprobante, impuestosPorTarifa);
    }

    /**
     * Calcula el desglose por tarifa a partir del documento ya adaptado — la misma cuenta que
     * hace {@code Voucher.of} para el Excel, {@link DetailedDocument} de por medio.
     */
    public static List<TasaImpuesto> tasasDe(name.julatec.ekonomi.tribunet.Documento documento) {
        final DetailedDocument detallado = DetailedDocument.of(documento);
        return TARIFAS.stream()
                .map(definicion -> {
                    final DetailedDocument.TaxAccumulated acumulado =
                            detallado.getTaxes().getOrElse(definicion.tarifa(), DetailedDocument.TaxAccumulated.empty);
                    return new TasaImpuesto(
                            definicion.tarifa().codigo, definicion.etiqueta(), acumulado.subTotal, acumulado.taxed);
                })
                .toList();
    }
}

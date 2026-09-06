package name.julatec.ekonomi.mcp;

import name.julatec.ekonomi.tribunet.DetalleServicio;
import name.julatec.ekonomi.tribunet.Documento;
import name.julatec.ekonomi.tribunet.ExoneracionType;
import name.julatec.ekonomi.tribunet.ImpuestoType;
import name.julatec.ekonomi.tribunet.LineaDetalle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link ComprobanteResumen#tasasDe} es lo nuevo detrás de "todos los tipos de impuesto, como
 * en el Excel" en la tabla de comprobantes: antes esa tabla traía un único {@code totalImpuesto}
 * agregado; ahora desglosa por las 11 tarifas de Hacienda, igual que {@code Voucher} (el Excel).
 * <p>
 * No usa un XML real ni {@code DocumentoAdapterService} —ese camino ya lo cubre
 * {@code DetailedDocumentTest} y {@code VoucherTest} con fixtures reales—; acá interesa
 * verificar específicamente que {@code tasasDe} arma las 11 filas en el orden del Excel, con la
 * etiqueta correcta, y que una tarifa sin líneas queda en cero y no en {@code null} —el
 * frontend le hace aritmética de formato a cada celda, y un {@code null} ahí revienta la vista,
 * no solo se ve raro—.
 */
class DesgloseImpuestoPorTarifaTest {

    /** Una línea con IVA (código "01", clasificación Iva) a la tarifa y montos dados. */
    private LineaDetalle lineaConIva(String codigoTarifaIVA, BigDecimal base, BigDecimal impuesto) {
        final ExoneracionType sinExoneracion = mock(ExoneracionType.class);
        when(sinExoneracion.getNumeroDocumento()).thenReturn(null);

        final ImpuestoType iva = mock(ImpuestoType.class);
        when(iva.getCodigo()).thenReturn("01");
        // getCodigoTarifaResuelto() es un default method que delega en este valor, pero un mock
        // de Mockito NO ejecuta el cuerpo real de un default method salvo que se le pida
        // explícitamente (p. ej. CALLS_REAL_METHODS) — sin stubear el resuelto directo, vuelve
        // null y DetailedDocument cae al fallback legado por valor numérico (Tarifa == null →
        // 0.0 → T01), metiendo la línea en la tarifa equivocada sin que ningún error lo avise.
        when(iva.getCodigoTarifaIVA()).thenReturn(codigoTarifaIVA);
        when(iva.getCodigoTarifaResuelto()).thenReturn(codigoTarifaIVA);
        when(iva.getMonto()).thenReturn(impuesto);
        when(iva.getExoneracion()).thenReturn(sinExoneracion);

        final LineaDetalle linea = mock(LineaDetalle.class);
        when(linea.getSubTotal()).thenReturn(base);
        when(linea.getMontoTotalLinea()).thenReturn(base.add(impuesto));
        when(linea.getImpuesto()).thenReturn(Stream.of(iva));
        return linea;
    }

    private Documento documentoCon(LineaDetalle... lineas) {
        final DetalleServicio detalle = mock(DetalleServicio.class);
        when(detalle.getLineaDetalle()).thenReturn(Stream.of(lineas));
        final Documento documento = mock(Documento.class);
        when(documento.getDetalleServicio()).thenReturn(detalle);
        return documento;
    }

    private ComprobanteResumen.TasaImpuesto porEtiqueta(
            List<ComprobanteResumen.TasaImpuesto> tasas, String etiqueta) {
        return tasas.stream()
                .filter(t -> t.etiqueta().equals(etiqueta))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no hay tarifa con etiqueta " + etiqueta));
    }

    @Test
    @DisplayName("trae las 11 tarifas siempre, en el orden del Excel, aunque el comprobante solo use dos")
    void traeLasOnceTarifasEnElOrdenDelExcel() {
        final Documento documento = documentoCon(
                lineaConIva("02", new BigDecimal("10000"), new BigDecimal("100")), // 1%
                lineaConIva("08", new BigDecimal("20000"), new BigDecimal("2600"))); // 13%

        final List<ComprobanteResumen.TasaImpuesto> tasas = ComprobanteResumen.tasasDe(documento);

        assertEquals(11, tasas.size());
        assertEquals(List.of(
                        "0% Art.32", "0.5%", "1%", "2%", "4%", "Transitorio 0%", "Transitorio 4%",
                        "8%", "13%", "Exenta", "0% sin crédito"),
                tasas.stream().map(ComprobanteResumen.TasaImpuesto::etiqueta).toList());
    }

    @Test
    @DisplayName("una tarifa con línea trae su base y su impuesto reales")
    void tarifaConLineaTraeSusMontos() {
        final Documento documento = documentoCon(
                lineaConIva("02", new BigDecimal("10000"), new BigDecimal("100")));

        final ComprobanteResumen.TasaImpuesto unoPorCiento =
                porEtiqueta(ComprobanteResumen.tasasDe(documento), "1%");

        assertEquals(0, new BigDecimal("10000").compareTo(unoPorCiento.baseImponible()));
        assertEquals(0, new BigDecimal("100").compareTo(unoPorCiento.impuesto()));
    }

    @Test
    @DisplayName("una tarifa sin ninguna línea queda en cero, no en null")
    void tarifaSinLineaQuedaEnCero() {
        final Documento documento = documentoCon(
                lineaConIva("02", new BigDecimal("10000"), new BigDecimal("100")));

        final ComprobanteResumen.TasaImpuesto trece =
                porEtiqueta(ComprobanteResumen.tasasDe(documento), "13%");

        assertEquals(0, BigDecimal.ZERO.compareTo(trece.baseImponible()));
        assertEquals(0, BigDecimal.ZERO.compareTo(trece.impuesto()));
    }
}

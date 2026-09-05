package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.report.csv.CsvBindByNameOrder;
import name.julatec.ekonomi.tribunet.Documento;
import name.julatec.ekonomi.tribunet.DocumentoAdapterService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * No existía ningún test para {@link Voucher} — este cubre la extensión de columnas a los
 * códigos de tarifa 01/05/06/09/10/11 (v4.4), verificando que cada línea cae en la columna
 * correcta y que no se pierde ni duplica dinero al partir el reporte más fino.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@SpringBootTest
class VoucherTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    DocumentoAdapterService documentoAdapterService;

    @Value("classpath:factura_multi_tarifa.xml")
    Resource facturaMultiTarifa;

    private Voucher voucherMultiTarifa() throws IOException {
        Optional<Documento> documento = documentoAdapterService.adapt(facturaMultiTarifa.getInputStream(), e -> fail());
        return Voucher.of(documento.get());
    }

    @Test
    void of_clasificaCadaLineaEnSuColumna() throws IOException {
        Voucher voucher = voucherMultiTarifa();

        // Línea 1: 1% (T02) — base 10000, impuesto 100.
        assertEquals(0, new BigDecimal("10000").compareTo(voucher.getTotalImpuestoT02()));
        assertEquals(0, new BigDecimal("100").compareTo(voucher.getTotalT02()));

        // Línea 2: 0.5% (T09) — base 20000, impuesto 100. Antes de este cambio caía en
        // Exonerado; ahora tiene su propia columna.
        assertEquals(0, new BigDecimal("20000").compareTo(voucher.getTotalImpuestoT09()));
        assertEquals(0, new BigDecimal("100").compareTo(voucher.getTotalT09()));

        // Línea 3: 0% sin derecho a crédito (T11) — base 5000, impuesto 0. Columna nueva de
        // v4.4, no existía antes de este cambio.
        assertEquals(0, new BigDecimal("5000").compareTo(voucher.getTotalImpuestoT11()));
        assertEquals(0, BigDecimal.ZERO.compareTo(voucher.getTotalT11()));

        // Ninguna línea debería haber caído en columnas ajenas.
        assertEquals(0, BigDecimal.ZERO.compareTo(voucher.getTotalExcento()));
        assertEquals(0, BigDecimal.ZERO.compareTo(voucher.getTotalExonerado()));
        assertEquals(0, BigDecimal.ZERO.compareTo(voucher.getTotalImpuestoT01()));
        assertEquals(0, BigDecimal.ZERO.compareTo(voucher.getTotalImpuestoT10()));
    }

    @Test
    void of_reconciliaConElTotalDelComprobante() throws IOException {
        // La suma de las bases imponibles de cada columna nueva debe seguir igualando el
        // total gravado del comprobante, y la suma de los impuestos su TotalImpuesto — partir
        // el reporte más fino no debe perder ni duplicar un colón.
        Voucher voucher = voucherMultiTarifa();

        BigDecimal sumaBaseImponible = voucher.getTotalImpuestoT02()
                .add(voucher.getTotalImpuestoT09())
                .add(voucher.getTotalImpuestoT11());
        assertEquals(0, new BigDecimal("35000").compareTo(sumaBaseImponible));

        BigDecimal sumaImpuesto = voucher.getTotalT02()
                .add(voucher.getTotalT09())
                .add(voucher.getTotalT11());
        assertEquals(0, new BigDecimal("200").compareTo(sumaImpuesto));

        assertEquals(0, new BigDecimal("35200").compareTo(voucher.getTotalComprobante()));
    }

    @Test
    void csvBindByNameOrder_noRompeLasColumnasExistentes() {
        // Las columnas que ya existían antes de este cambio deben seguir con el mismo nombre
        // de encabezado — libros-contables-2025 consume este reporte por nombre de columna.
        List<String> columnas = List.of(Voucher.class.getAnnotation(CsvBindByNameOrder.class).value());

        List<String> columnasPreexistentes = List.of(
                "Fecha", "Consecutivo", "Emisor", "Nombre Emisor", "Receptor", "Nombre Receptor",
                "Total Excento", "Total Exonerado",
                "Impuesto 1%", "Base Imponible 1%",
                "Impuesto 2%", "Base Imponible 2%",
                "Impuesto 4%", "Base Imponible 4%",
                "Impuesto 8%", "Base Imponible 8%",
                "Impuesto 13%", "Base Imponible 13%",
                "Base Imponible Devuelto", "Total Otros Cargos", "Total Comprobante", "Clave", "Moneda");

        for (String columna : columnasPreexistentes) {
            assertTrue(columnas.contains(columna), "falta la columna preexistente: " + columna);
        }

        // Las 6 columnas nuevas de v4.4 (01/05/06/09/10/11) deben estar presentes.
        List<String> columnasNuevas = List.of(
                "Tarifa 0% Art.32", "Base Imponible 0% Art.32",
                "Transitorio 0%", "Base Imponible Trans. 0%",
                "Transitorio 4%", "Base Imponible Trans. 4%",
                "Impuesto 0.5%", "Base Imponible 0.5%",
                "Tarifa Exenta", "Base Imponible Exenta",
                "Tarifa 0% sin crédito", "Base Imponible 0% sin crédito");
        for (String columna : columnasNuevas) {
            assertTrue(columnas.contains(columna), "falta la columna nueva: " + columna);
        }
    }

    @Test
    void toWorkbook_noLanzaExcepcionConVariosVouchers() throws Exception {
        Voucher voucher = voucherMultiTarifa();
        Workbook workbook = Voucher.toWorkbook(List.of(voucher, voucher));
        assertNotNull(workbook);
        Sheet sheet = workbook.getSheetAt(0);
        // Encabezado + 2 filas de datos.
        assertEquals(3, sheet.getPhysicalNumberOfRows());
    }
}

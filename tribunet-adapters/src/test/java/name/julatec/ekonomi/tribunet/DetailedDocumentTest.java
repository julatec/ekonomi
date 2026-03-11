package name.julatec.ekonomi.tribunet;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@SpringBootTest
class DetailedDocumentTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    DocumentoAdapterService documentoAdapterService;

    @Value("classpath:factura.xml")
    Resource factura;

    @Value("classpath:facturaCompra.xml")
    Resource facturaCompra;

    @Value("classpath:notaCreditov42.xml")
    Resource notaCreditoV42;

    @Value("classpath:notaCreditov43.xml")
    Resource notaCreditoV43;

    @Value("classpath:notaDebito.xml")
    Resource notaDebito;

    @Value("classpath:factura_t01.xml")
    Resource facturaT01;

    @Value("classpath:factura_t02.xml")
    Resource facturaT02;

    @Value("classpath:factura_t03.xml")
    Resource facturaT03;

    @Value("classpath:factura_t04.xml")
    Resource facturaT04;

    @Value("classpath:factura_t09.xml")
    Resource facturaT09;

    @Value("classpath:factura_t10.xml")
    Resource facturaT10;

    @Value("classpath:factura_t11.xml")
    Resource facturaT11;

    @Test
    void adaptFactura() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(factura.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("331629.81552"),
                        new BigDecimal("374741.69154"),
                        new BigDecimal("43111.87602")),
                detailedDocument.getTaxes().get(FactorIVA.T08));
    }

    @Test
    void adaptFacturaCompra() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaCompra.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("251142.60"),
                        new BigDecimal("283791.14"),
                        new BigDecimal("32648.54")),
                detailedDocument.getTaxes().get(FactorIVA.T08));
    }

    @Test
    void adaptNotaCreditoV42() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(notaCreditoV42.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("435200.00"),
                        new BigDecimal("435200.00"),
                        new BigDecimal("0")),
                detailedDocument.getTaxes().get(FactorIVA.Excento));
    }

    @Test
    void adaptNotaCreditoV43() throws IOException {
        // notaCreditov43.xml has CodigoTarifa=05 (Transitorio 0%)
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(notaCreditoV43.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("830000.0"),
                        new BigDecimal("830000.0"),
                        new BigDecimal("0.0")),
                detailedDocument.getTaxes().get(FactorIVA.T05));
    }

    @Test
    void adaptNotaDebito() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(notaDebito.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("729.00000"),
                        new BigDecimal("729.00000"),
                        new BigDecimal("0")),
                detailedDocument.getTaxes().get(FactorIVA.Excento));
    }

    // --- Nota 8.1: Tests for each CodigoTarifa ---

    @Test
    void adaptFacturaT01_TarifaCeroArticulo32() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT01.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        DetailedDocument.TaxAccumulated t01 = detailedDocument.getTaxes().get(FactorIVA.T01);
        assertNotNull(t01, "Should classify under T01 (0% Art.32)");
        assertEquals(0, new BigDecimal("10000").compareTo(t01.subTotal));
        assertEquals(0, BigDecimal.ZERO.compareTo(t01.taxed));
    }

    @Test
    void adaptFacturaT02_TarifaReducida1() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT02.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        DetailedDocument.TaxAccumulated t02 = detailedDocument.getTaxes().get(FactorIVA.T02);
        assertNotNull(t02, "Should classify under T02 (1%)");
        assertEquals(0, new BigDecimal("10000").compareTo(t02.subTotal));
        assertEquals(0, new BigDecimal("100").compareTo(t02.taxed));
    }

    @Test
    void adaptFacturaT03_TarifaReducida2() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT03.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        DetailedDocument.TaxAccumulated t03 = detailedDocument.getTaxes().get(FactorIVA.T03);
        assertNotNull(t03, "Should classify under T03 (2%)");
        assertEquals(0, new BigDecimal("10000").compareTo(t03.subTotal));
        assertEquals(0, new BigDecimal("200").compareTo(t03.taxed));
    }

    @Test
    void adaptFacturaT04_TarifaReducida4() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT04.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        DetailedDocument.TaxAccumulated t04 = detailedDocument.getTaxes().get(FactorIVA.T04);
        assertNotNull(t04, "Should classify under T04 (4%)");
        assertEquals(0, new BigDecimal("10000").compareTo(t04.subTotal));
        assertEquals(0, new BigDecimal("400").compareTo(t04.taxed));
    }

    @Test
    void adaptFacturaT09_TarifaReducida05() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT09.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        DetailedDocument.TaxAccumulated t09 = detailedDocument.getTaxes().get(FactorIVA.T09);
        assertNotNull(t09, "Should classify under T09 (0.5%)");
        assertEquals(0, new BigDecimal("10000").compareTo(t09.subTotal));
        assertEquals(0, new BigDecimal("50").compareTo(t09.taxed));
    }

    @Test
    void adaptFacturaT10_TarifaExenta() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT10.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        // T10 has no Impuesto block, so it defaults to Excento bucket
        DetailedDocument.TaxAccumulated excento = detailedDocument.getTaxes().get(FactorIVA.Excento);
        assertNotNull(excento, "Exempt items without Impuesto block should classify as Excento");
        assertEquals(0, new BigDecimal("10000").compareTo(excento.subTotal));
        assertEquals(0, BigDecimal.ZERO.compareTo(excento.taxed));
    }

    @Test
    void adaptFacturaT11_TarifaCeroSinCredito() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT11.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        DetailedDocument.TaxAccumulated t11 = detailedDocument.getTaxes().get(FactorIVA.T11);
        assertNotNull(t11, "Should classify under T11 (0% sin credito)");
        assertEquals(0, new BigDecimal("10000").compareTo(t11.subTotal));
        assertEquals(0, BigDecimal.ZERO.compareTo(t11.taxed));
    }

    // --- Nota 8: ImpuestoType.Codigo reverse mapping ---

    @Test
    void codigoOfReturnsCorrectValues() {
        assertEquals(ImpuestoType.Codigo.ValorAgregado, ImpuestoType.Codigo.of("01"));
        assertEquals(ImpuestoType.Codigo.SelectivoDeConsumo, ImpuestoType.Codigo.of("02"));
        assertEquals(ImpuestoType.Codigo.Combustivos, ImpuestoType.Codigo.of("03"));
        assertEquals(ImpuestoType.Codigo.BebidasAlcoholicas, ImpuestoType.Codigo.of("04"));
        assertEquals(ImpuestoType.Codigo.BebidasEnvasadas, ImpuestoType.Codigo.of("05"));
        assertEquals(ImpuestoType.Codigo.ProductosDeTabaco, ImpuestoType.Codigo.of("06"));
        assertEquals(ImpuestoType.Codigo.ValorAgregadoEspecial, ImpuestoType.Codigo.of("07"));
        assertEquals(ImpuestoType.Codigo.ValorAgregadoUsados, ImpuestoType.Codigo.of("08"));
        assertEquals(ImpuestoType.Codigo.Cemento, ImpuestoType.Codigo.of("12"));
        assertEquals(ImpuestoType.Codigo.Otros, ImpuestoType.Codigo.of("99"));
    }

    @Test
    void codigoClasificacionIsCorrect() {
        // BASE_IMPONIBLE: specific consumption taxes
        assertEquals(ImpuestoType.Codigo.Clasificacion.BASE_IMPONIBLE,
                ImpuestoType.Codigo.SelectivoDeConsumo.getClasificacion());
        assertEquals(ImpuestoType.Codigo.Clasificacion.BASE_IMPONIBLE,
                ImpuestoType.Codigo.Combustivos.getClasificacion());
        assertEquals(ImpuestoType.Codigo.Clasificacion.BASE_IMPONIBLE,
                ImpuestoType.Codigo.BebidasAlcoholicas.getClasificacion());
        assertEquals(ImpuestoType.Codigo.Clasificacion.BASE_IMPONIBLE,
                ImpuestoType.Codigo.BebidasEnvasadas.getClasificacion());
        assertEquals(ImpuestoType.Codigo.Clasificacion.BASE_IMPONIBLE,
                ImpuestoType.Codigo.ProductosDeTabaco.getClasificacion());
        assertEquals(ImpuestoType.Codigo.Clasificacion.BASE_IMPONIBLE,
                ImpuestoType.Codigo.Cemento.getClasificacion());
        assertEquals(ImpuestoType.Codigo.Clasificacion.BASE_IMPONIBLE,
                ImpuestoType.Codigo.Otros.getClasificacion());

        // IVA: valor agregado family
        assertEquals(ImpuestoType.Codigo.Clasificacion.IVA,
                ImpuestoType.Codigo.ValorAgregado.getClasificacion());
        assertEquals(ImpuestoType.Codigo.Clasificacion.IVA,
                ImpuestoType.Codigo.ValorAgregadoEspecial.getClasificacion());
        assertEquals(ImpuestoType.Codigo.Clasificacion.IVA,
                ImpuestoType.Codigo.ValorAgregadoUsados.getClasificacion());

        // GUARD: sentinel values
        assertEquals(ImpuestoType.Codigo.Clasificacion.GUARD,
                ImpuestoType.Codigo.Empty.getClasificacion());
        assertEquals(ImpuestoType.Codigo.Clasificacion.GUARD,
                ImpuestoType.Codigo.BaseImponible.getClasificacion());
        assertEquals(ImpuestoType.Codigo.Clasificacion.GUARD,
                ImpuestoType.Codigo.OtrosCargos.getClasificacion());
        assertEquals(ImpuestoType.Codigo.Clasificacion.GUARD,
                ImpuestoType.Codigo.Total.getClasificacion());
    }
}

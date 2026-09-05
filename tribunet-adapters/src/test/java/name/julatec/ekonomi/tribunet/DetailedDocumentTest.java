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

    @Value("classpath:factura_v44_t09.xml")
    Resource facturaV44T09;

    @Value("classpath:factura_v44_t10.xml")
    Resource facturaV44T10;

    @Test
    void adaptFactura() throws IOException {
        // CodigoTarifa=08 (Tarifa general 13%) en las 20 líneas — antes se leía por el valor
        // numérico de Tarifa y caía en el alias F13; ahora se resuelve por código a T08.
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
        // v4.2: la línea no trae nodo <Impuesto> en absoluto (no existe CodigoTarifa en ese
        // esquema) — sigue clasificando como Excento, sin pasar por FactorIVA.
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
        // Esta línea trae CodigoTarifa=05 (Transitorio 0%) explícito. Con el mecanismo viejo
        // (bucket por valor numérico, Tarifa=0) caía en el mismo cajón genérico que un
        // documento sin dato de tarifa — Excento. Con fromCodigo("05") queda correctamente
        // identificado como T05: es el caso real que demuestra por qué el código es preferible
        // al valor numérico, no un ejemplo sintético.
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

    // --- Códigos de tarifa v4.3 (CodigoTarifa), fixtures antes huérfanos ---

    @Test
    void adaptFacturaT01() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT01.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("10000"), new BigDecimal("10000"), new BigDecimal("0")),
                detailedDocument.getTaxes().get(FactorIVA.T01));
    }

    @Test
    void adaptFacturaT02() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT02.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("10000"), new BigDecimal("10100"), new BigDecimal("100")),
                detailedDocument.getTaxes().get(FactorIVA.T02));
    }

    @Test
    void adaptFacturaT03() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT03.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("10000"), new BigDecimal("10200"), new BigDecimal("200")),
                detailedDocument.getTaxes().get(FactorIVA.T03));
    }

    @Test
    void adaptFacturaT04() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT04.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("10000"), new BigDecimal("10400"), new BigDecimal("400")),
                detailedDocument.getTaxes().get(FactorIVA.T04));
    }

    @Test
    void adaptFacturaT09_noLongerMisclassifiedAsExonerado() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT09.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("10000"), new BigDecimal("10050"), new BigDecimal("50")),
                detailedDocument.getTaxes().get(FactorIVA.T09));
        assertNull(detailedDocument.getTaxes().get(FactorIVA.Exonerado),
                "una tarifa de 0.5% no es una exoneración legal");
    }

    @Test
    void adaptFacturaT10_sinNodoImpuesto_siGueSiendoExcento() throws IOException {
        // Este fixture no trae <Impuesto> en la línea: es "bien no gravado", distinto de una
        // "Tarifa Exenta" (código 10) declarada explícitamente — ver adaptFacturaV44T10.
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT10.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("10000"), new BigDecimal("10000"), new BigDecimal("0")),
                detailedDocument.getTaxes().get(FactorIVA.Excento));
    }

    @Test
    void adaptFacturaT11_distinctFromT10() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaT11.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("10000"), new BigDecimal("10000"), new BigDecimal("0")),
                detailedDocument.getTaxes().get(FactorIVA.T11));
        assertNull(detailedDocument.getTaxes().get(FactorIVA.T10));
    }

    // --- v4.4: CodigoTarifaIVA (campo renombrado) — el camino que updates nunca probó ---

    @Test
    void adaptFacturaV44T09_resuelveCodigoTarifaIVA() throws IOException {
        // Prueba de extremo a extremo: v4.4 no tiene CodigoTarifa, tiene CodigoTarifaIVA.
        // Si getCodigoTarifaResuelto() no encontrara este campo, caería al valor numérico
        // (0.5, que sí resuelve a T09 igual) — por eso además se confirma que NO cae a
        // Excento/Otros, que es lo que pasaría si el nombre del método no se hubiera agregado.
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaV44T09.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("10000"), new BigDecimal("10050"), new BigDecimal("50")),
                detailedDocument.getTaxes().get(FactorIVA.T09));
    }

    @Test
    void adaptFacturaV44T10_codigoExplicito_distintoDeAusenciaDeImpuesto() throws IOException {
        // A diferencia de factura_t10.xml (sin nodo <Impuesto>), este documento SÍ declara
        // <Impuesto><CodigoTarifaIVA>10</CodigoTarifaIVA></Impuesto> — una tarifa 0% real y
        // válida solo desde v4.4. El valor numérico (Tarifa=0) por sí solo sería indistinguible
        // de T01/T05/T11; solo el código lo resuelve.
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaV44T10.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("10000"), new BigDecimal("10000"), new BigDecimal("0")),
                detailedDocument.getTaxes().get(FactorIVA.T10));
    }

    // --- Diferencial por versión: documenta que v4.2/v4.3/v4.4 resuelven distinto ---

    @Test
    void codigoTarifaResuelto_difiereSegunVersionDelEsquema() throws IOException {
        ImpuestoType impuestoV42 = firstImpuesto(notaCreditoV42);
        ImpuestoType impuestoV43 = firstImpuesto(facturaT09);
        ImpuestoType impuestoV44 = firstImpuesto(facturaV44T09);

        // v4.2: no existe el campo en absoluto (o no hay línea con Impuesto en este fixture).
        assertTrue(impuestoV42 == null || impuestoV42.getCodigoTarifaResuelto() == null);
        assertEquals("09", impuestoV43.getCodigoTarifaResuelto());
        assertEquals("09", impuestoV44.getCodigoTarifaResuelto());
    }

    private ImpuestoType firstImpuesto(Resource resource) throws IOException {
        Documento documento = documentoAdapterService.adapt(resource.getInputStream(), e -> fail()).get();
        return documento.getDetalleServicio().getLineaDetalle()
                .flatMap(linea -> linea.getImpuesto())
                .findFirst()
                .orElse(null);
    }
}

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

    @Test
    void adaptFactura() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(factura.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("331629.81552"),
                        new BigDecimal("374741.69154"),
                        new BigDecimal("43111.87602")),
                detailedDocument.getTaxes().get(FactorIVA.F13));
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
                detailedDocument.getTaxes().get(FactorIVA.F13));
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
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(notaCreditoV43.getInputStream(), e -> fail());
        DetailedDocument detailedDocument = DetailedDocument.of(optionalDocumento.get());
        assertEquals(1, detailedDocument.getTaxes().size());
        assertEquals(new DetailedDocument.TaxAccumulated(
                        new BigDecimal("830000.0"),
                        new BigDecimal("830000.0"),
                        new BigDecimal("0.0")),
                detailedDocument.getTaxes().get(FactorIVA.Excento));
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
}
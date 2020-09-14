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
class DocumentoAdapterServiceTest {

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
        assertFalse(optionalDocumento.isEmpty());
        Documento documento = optionalDocumento.get();
        assertEquals("50610012000310231549000100004010000054357101884339", documento.getClave());
        assertEquals(1578636000000L, documento.getFechaEmisionAsDate().getTime());
        assertEquals("00100004010000044459", documento.getNumeroConsecutivo());
        assertEquals("INVERSIONES ABC", documento.getEmisor().getNombre());
        assertEquals("55102565590", documento.getEmisor().getIdentificacion().getNumero());
        assertEquals("02", documento.getEmisor().getIdentificacion().getTipo());
        assertEquals("Test Testing", documento.getReceptor().getNombre());
        assertEquals("501230456", documento.getReceptor().getIdentificacion().getNumero());
        assertEquals("01", documento.getReceptor().getIdentificacion().getTipo());
        assertEquals(20, documento.getDetalleServicio().getLineaDetalle().count());
        assertEquals(new BigDecimal("374741.69154"), documento.getResumenFactura().getTotalComprobante());
        assertTrue(documento instanceof Factura);
    }

    @Test
    void adaptFacturaCompra() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(facturaCompra.getInputStream(), e -> fail());
        assertFalse(optionalDocumento.isEmpty());
        Documento documento = optionalDocumento.get();
        assertEquals("50628082000310157848300100002080000000003105210123", documento.getClave());
        assertEquals(1598660739000L, documento.getFechaEmisionAsDate().getTime());
        assertEquals("00100001080090000003", documento.getNumeroConsecutivo());
        assertEquals("ABC DEF GHT", documento.getEmisor().getNombre());
        assertEquals("6101004055", documento.getEmisor().getIdentificacion().getNumero());
        assertEquals("02", documento.getEmisor().getIdentificacion().getTipo());
        assertEquals("XYZ WWW GHJ", documento.getReceptor().getNombre());
        assertEquals("9101498473", documento.getReceptor().getIdentificacion().getNumero());
        assertEquals("02", documento.getReceptor().getIdentificacion().getTipo());
        assertEquals(2, documento.getDetalleServicio().getLineaDetalle().count());
        assertEquals(new BigDecimal("283791.14"), documento.getResumenFactura().getTotalComprobante());
        assertTrue(documento instanceof FacturaCompra);
    }

    @Test
    void adaptNotaCreditoV42() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(notaCreditoV42.getInputStream(), e -> fail());
        assertFalse(optionalDocumento.isEmpty());
        Documento documento = optionalDocumento.get();
        assertEquals("50623041900310157848300100001030000000002114564912", documento.getClave());
        assertEquals(1556059710000L, documento.getFechaEmisionAsDate().getTime());
        assertEquals("00100001030000360002", documento.getNumeroConsecutivo());
        assertEquals("ABC DEF SA", documento.getEmisor().getNombre());
        assertEquals("5694517529", documento.getEmisor().getIdentificacion().getNumero());
        assertEquals("02", documento.getEmisor().getIdentificacion().getTipo());
        assertEquals("FRGKKK MILLLD ERTROT", documento.getReceptor().getNombre());
        assertEquals("605971281", documento.getReceptor().getIdentificacion().getNumero());
        assertEquals("01", documento.getReceptor().getIdentificacion().getTipo());
        assertEquals(1, documento.getDetalleServicio().getLineaDetalle().count());
        assertEquals(new BigDecimal("435200.00"), documento.getResumenFactura().getTotalComprobante());
        assertTrue(documento instanceof NotaCredito);
    }

    @Test
    void adaptNotaCreditoV43() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(notaCreditoV43.getInputStream(), e -> fail());
        assertFalse(optionalDocumento.isEmpty());
        Documento documento = optionalDocumento.get();
        assertEquals("50613022000310159274125992285911700000048982220086", documento.getClave());
        assertEquals(1581613303000L, documento.getFechaEmisionAsDate().getTime());
        assertEquals("00100159222855229928", documento.getNumeroConsecutivo());
        assertEquals("ABCD EFGGGD S.A.", documento.getEmisor().getNombre());
        assertEquals("5014529285", documento.getEmisor().getIdentificacion().getNumero());
        assertEquals("02", documento.getEmisor().getIdentificacion().getTipo());
        assertEquals("AAADDDADD", documento.getReceptor().getNombre());
        assertEquals("3251485921", documento.getReceptor().getIdentificacion().getNumero());
        assertEquals("02", documento.getReceptor().getIdentificacion().getTipo());
        assertEquals(1, documento.getDetalleServicio().getLineaDetalle().count());
        assertEquals(new BigDecimal("830000.0"), documento.getResumenFactura().getTotalComprobante());
        assertTrue(documento instanceof NotaCredito);
    }

    @Test
    void adaptNotaDebito() throws IOException {
        Optional<Documento> optionalDocumento = documentoAdapterService.adapt(notaDebito.getInputStream(), e -> fail());
        assertFalse(optionalDocumento.isEmpty());
        Documento documento = optionalDocumento.get();
        assertEquals("50630096589521459221113900844005498100051891802841", documento.getClave());
        assertEquals(1569895766000L, documento.getFechaEmisionAsDate().getTime());
        assertEquals("00100001020005485062", documento.getNumeroConsecutivo());
        assertEquals("ABCDFEF, S.A.", documento.getEmisor().getNombre());
        assertEquals("3126529222", documento.getEmisor().getIdentificacion().getNumero());
        assertEquals("02", documento.getEmisor().getIdentificacion().getTipo());
        assertEquals("ABC DEF EFG", documento.getReceptor().getNombre());
        assertEquals("219292558", documento.getReceptor().getIdentificacion().getNumero());
        assertEquals("01", documento.getReceptor().getIdentificacion().getTipo());
        assertEquals(1, documento.getDetalleServicio().getLineaDetalle().count());
        assertEquals(new BigDecimal("729.00000"), documento.getResumenFactura().getTotalComprobante());
        assertTrue(documento instanceof NotaDebito);
    }

    @Test
    void supportedNamespaces() {
        assertFalse(documentoAdapterService.supportedNamespaces().isEmpty());
    }
}
package name.julatec.ekonomi.tribunet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    @Value("classpath:tiquete_v44_sin_receptor.xml")
    Resource tiqueteSinReceptor;

    // ────────────────────────────────────────────────────────────────────────────────
    // Basura pegada después del cierre de la raíz.
    //
    // El 30 set 2026 entró a `facturas.tribuconta@julatec.name` un comprobante que Xerces
    // rechazaba con «Content is not allowed in trailing section» (línea 14, columna 4714).
    // Reintentó cuatro veces al día durante una semana y nunca entró: la parte válida
    // estaba completa, pero el documento se descartaba entero por lo que venía pegado
    // DESPUÉS del cierre. Es el único documento que ha fallado al leerse.
    // ────────────────────────────────────────────────────────────────────────────────

    /** El mismo comprobante bueno, con algo pegado al final. */
    private byte[] conBasuraAlFinal(Resource recurso, String basura) throws IOException {
        final byte[] bueno = recurso.getInputStream().readAllBytes();
        final byte[] cola = basura.getBytes(StandardCharsets.UTF_8);
        final byte[] roto = new byte[bueno.length + cola.length];
        System.arraycopy(bueno, 0, roto, 0, bueno.length);
        System.arraycopy(cola, 0, roto, bueno.length, cola.length);
        return roto;
    }

    @Test
    void comprobanteConBasuraPegadaAlFinalSeRecupera() throws IOException {
        final byte[] roto = conBasuraAlFinal(factura, "\n<<< basura del servidor de correo >>>\n");

        final List<Throwable> errores = new ArrayList<>();
        final Optional<Documento> documento =
                documentoAdapterService.adapt(new ByteArrayInputStream(roto), errores::add);

        assertTrue(errores.isEmpty(), () -> "no debería reportar error: " + errores);
        assertTrue(documento.isPresent(), "el comprobante tenía que recuperarse");
        // Y se recupera COMPLETO, no a medias: mismos datos que el fixture sano.
        assertEquals("50610012000310231549000100004010000054357101884339", documento.get().getClave());
        assertEquals(20, documento.get().getDetalleServicio().getLineaDetalle().count());
        assertEquals(new BigDecimal("374741.69154"),
                documento.get().getResumenFactura().getTotalComprobante());
    }

    @Test
    void dosComprobantesConcatenadosSeQuedaConElPrimero() throws IOException {
        // El otro modo en que aparece: dos documentos en un mismo archivo. Se corta en el
        // PRIMER cierre; cortar en el último dejaría los dos y seguiría sin ser válido.
        final String segundo = new String(
                facturaCompra.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        final byte[] roto = conBasuraAlFinal(factura, segundo);

        final List<Throwable> errores = new ArrayList<>();
        final Optional<Documento> documento =
                documentoAdapterService.adapt(new ByteArrayInputStream(roto), errores::add);

        assertTrue(errores.isEmpty(), () -> "no debería reportar error: " + errores);
        assertTrue(documento.isPresent());
        assertEquals("50610012000310231549000100004010000054357101884339", documento.get().getClave());
    }

    @Test
    void espaciosYSaltosAlFinalNoSonBasura() throws IOException {
        // Un XML válido puede llevar espacios después del cierre. No hay nada que recuperar
        // porque nunca falló — y el recorte no tiene que activarse.
        final byte[] conEspacios = conBasuraAlFinal(factura, "\n\n   \t\n");
        assertNull(ColaXml.recortarTrasElCierreDeLaRaiz(conEspacios),
                "solo espacios después del cierre no es basura que recortar");

        final Optional<Documento> documento =
                documentoAdapterService.adapt(new ByteArrayInputStream(conEspacios), e -> fail());
        assertTrue(documento.isPresent());
    }

    @Test
    void unXmlRotoDeVerdadSigueFallando() throws IOException {
        // El recorte no puede convertirse en «acepta cualquier cosa»: si lo que está mal
        // está ANTES del cierre, tiene que seguir fallando, y con el error original.
        final byte[] roto = "<FacturaElectronica><sin cerrar</FacturaElectronica> basura"
                .getBytes(StandardCharsets.UTF_8);

        final List<Throwable> errores = new ArrayList<>();
        final Optional<Documento> documento =
                documentoAdapterService.adapt(new ByteArrayInputStream(roto), errores::add);

        assertTrue(documento.isEmpty(), "un documento mal formado de verdad no se acepta");
        assertEquals(1, errores.size(), "y reporta el error, uno solo");
    }

    @Test
    void elRecorteRespetaLaCodificacionDelDocumento() {
        // Se trabaja sobre bytes justamente para no recodificar. Un documento declarado en
        // ISO-8859-1 con una eñe tiene que salir del recorte byte por byte idéntico.
        final byte[] original =
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><Raiz><N>Pe\u00f1a</N></Raiz>"
                        .getBytes(StandardCharsets.ISO_8859_1);
        final byte[] roto = new byte[original.length + 7];
        System.arraycopy(original, 0, roto, 0, original.length);
        System.arraycopy("<basura".getBytes(StandardCharsets.US_ASCII), 0, roto, original.length, 7);

        final byte[] recortado = ColaXml.recortarTrasElCierreDeLaRaiz(roto);
        assertArrayEquals(original, recortado);
    }

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

    /**
     * Un tiquete sin nodo Receptor tiene que adaptarse igual.
     * <p>
     * En {@code TiqueteElectronico_V4.4.xsd} el Receptor es {@code minOccurs="0"}: un tiquete a
     * consumidor final no lleva identificación de quien compra, y es el documento que más emite
     * un comercio de mostrador. Antes de este arreglo, el constructor del adaptador generado
     * hacía {@code new IdentificacionFactory...(target.getIdentificacion())} sobre un
     * {@code target} nulo y lanzaba {@link NullPointerException}. Como
     * {@link DocumentoAdapterService#adapt(org.w3c.dom.Document)} la atrapa y devuelve vacío, el
     * comprobante se perdía en silencio: 120 documentos el 5 de setiembre de 2026 en producción,
     * todos con la misma traza.
     * <p>
     * Lo que se fija es que el documento entre completo y que la ausencia del receptor se
     * exprese como campos nulos, no como una excepción ni como un documento que no existe.
     */
    @Test
    void adaptTiqueteSinReceptor() throws IOException {
        Optional<Documento> optionalDocumento =
                documentoAdapterService.adapt(tiqueteSinReceptor.getInputStream(), e -> fail());
        assertFalse(optionalDocumento.isEmpty(), "un tiquete sin receptor no debe perderse");
        Documento documento = optionalDocumento.get();
        assertEquals("50610012000310231549000100004010000054357101884012", documento.getClave());
        assertEquals("00100004010000040012", documento.getNumeroConsecutivo());
        assertEquals("INVERSIONES ABC", documento.getEmisor().getNombre());
        assertEquals("55102565590", documento.getEmisor().getIdentificacion().getNumero());
        // El receptor sigue siendo un adaptador —nunca nulo— pero todo lo suyo es nulo. Esa es
        // la forma que el resto del sistema ya sabe leer: `Voucher` encadena
        // getReceptor().getIdentificacion().getNumero() sin comprobar nada.
        assertNotNull(documento.getReceptor());
        assertNull(documento.getReceptor().getNombre());
        assertNotNull(documento.getReceptor().getIdentificacion());
        assertNull(documento.getReceptor().getIdentificacion().getNumero());
        assertEquals(1, documento.getDetalleServicio().getLineaDetalle().count());
    }

    @Test
    void supportedNamespaces() {
        assertFalse(documentoAdapterService.supportedNamespaces().isEmpty());
    }
}
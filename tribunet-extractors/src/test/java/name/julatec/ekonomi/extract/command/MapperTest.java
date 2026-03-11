package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.*;
import name.julatec.ekonomi.tribunet.storage.ClaveNota;
import name.julatec.ekonomi.tribunet.storage.Mensaje;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    private EmisorMapper emisorMapper;
    private ReceptorMapper receptorMapper;
    private ResumenMapper resumenMapper;
    private DocumentoMapper documentoMapper;
    private FacturaMapper facturaMapper;
    private NotaCreditoMapper notaCreditoMapper;
    private NotaDebitoMapper notaDebitoMapper;
    private FacturaCompraMapper facturaCompraMapper;
    private FacturaExportacionMapper facturaExportacionMapper;
    private MensajeHaciendaMapper mensajeHaciendaMapper;
    private MensajeReceptorMapper mensajeReceptorMapper;

    @BeforeEach
    void setUp() {
        emisorMapper = new EmisorMapper();
        receptorMapper = new ReceptorMapper();
        resumenMapper = new ResumenMapper();
        documentoMapper = new DocumentoMapper(emisorMapper, receptorMapper, resumenMapper);
        facturaMapper = new FacturaMapper(documentoMapper);
        notaCreditoMapper = new NotaCreditoMapper();
        notaCreditoMapper.documentoMapper = documentoMapper;
        notaDebitoMapper = new NotaDebitoMapper();
        notaDebitoMapper.documentoMapper = documentoMapper;
        facturaCompraMapper = new FacturaCompraMapper();
        facturaCompraMapper.documentoMapper = documentoMapper;
        facturaExportacionMapper = new FacturaExportacionMapper(documentoMapper);
        mensajeHaciendaMapper = new MensajeHaciendaMapper();
        mensajeReceptorMapper = new MensajeReceptorMapper();
    }

    // ---- Helper factories ----

    private static Identificacion identificacion(String numero, String tipo) {
        return new Identificacion() {
            @Override public String getTipo() { return tipo; }
            @Override public String getNumero() { return numero; }
        };
    }

    private static Emisor emisor(String nombre, String numero, String tipo) {
        return new Emisor() {
            @Override public String getNombre() { return nombre; }
            @Override public Identificacion getIdentificacion() { return identificacion(numero, tipo); }
        };
    }

    private static Receptor receptor(String nombre, Identificacion id) {
        return new Receptor() {
            @Override public String getNombre() { return nombre; }
            @Override public Identificacion getIdentificacion() { return id; }
        };
    }

    private static Resumen resumen(String moneda, BigDecimal cambio, BigDecimal impuesto, BigDecimal total) {
        return new Resumen() {
            @Override public String getCodigoMoneda() { return moneda; }
            @Override public BigDecimal getTipoCambio() { return cambio; }
            @Override public BigDecimal getTotalImpuesto() { return impuesto; }
            @Override public BigDecimal getTotalComprobante() { return total; }
        };
    }

    private static XMLGregorianCalendar cal2023() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(2023, 6, 15, 10, 0, 0, 0, 0);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Factura factura(String clave, String consecutivo) {
        Emisor e = emisor("Test Emisor", "12345", "01");
        Receptor r = receptor("Test Receptor", identificacion("67890", "02"));
        Resumen res = resumen("CRC", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN);
        return new Factura() {
            @Override public String getClave() { return clave; }
            @Override public String getNumeroConsecutivo() { return consecutivo; }
            @Override public javax.xml.datatype.XMLGregorianCalendar getFechaEmision() { return cal2023(); }
            @Override public Emisor getEmisor() { return e; }
            @Override public Receptor getReceptor() { return r; }
            @Override public Resumen getResumenFactura() { return res; }
            @Override public DetalleServicio getDetalleServicio() { return Stream::empty; }
            @Override public Stream<InformacionReferencia> getInformacionReferencia() { return Stream.empty(); }
        };
    }

    private static NotaCredito notaCredito(String clave, String consecutivo) {
        Emisor e = emisor("Emisor NC", "11111", "01");
        Receptor r = receptor("Receptor NC", identificacion("22222", "02"));
        Resumen res = resumen("CRC", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN);
        return new NotaCredito() {
            @Override public String getClave() { return clave; }
            @Override public String getNumeroConsecutivo() { return consecutivo; }
            @Override public javax.xml.datatype.XMLGregorianCalendar getFechaEmision() { return cal2023(); }
            @Override public Emisor getEmisor() { return e; }
            @Override public Receptor getReceptor() { return r; }
            @Override public Resumen getResumenFactura() { return res; }
            @Override public DetalleServicio getDetalleServicio() { return Stream::empty; }
            @Override public Stream<InformacionReferencia> getInformacionReferencia() { return Stream.empty(); }
        };
    }

    private static NotaDebito notaDebito(String clave, String consecutivo) {
        Emisor e = emisor("Emisor ND", "33333", "01");
        Receptor r = receptor("Receptor ND", identificacion("44444", "02"));
        Resumen res = resumen("CRC", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN);
        return new NotaDebito() {
            @Override public String getClave() { return clave; }
            @Override public String getNumeroConsecutivo() { return consecutivo; }
            @Override public javax.xml.datatype.XMLGregorianCalendar getFechaEmision() { return cal2023(); }
            @Override public Emisor getEmisor() { return e; }
            @Override public Receptor getReceptor() { return r; }
            @Override public Resumen getResumenFactura() { return res; }
            @Override public DetalleServicio getDetalleServicio() { return Stream::empty; }
            @Override public Stream<InformacionReferencia> getInformacionReferencia() { return Stream.empty(); }
        };
    }

    private static FacturaCompra facturaCompra(String clave, String consecutivo) {
        Emisor e = emisor("Emisor FC", "55555", "01");
        Receptor r = receptor("Receptor FC", identificacion("66666", "02"));
        Resumen res = resumen("CRC", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN);
        return new FacturaCompra() {
            @Override public String getClave() { return clave; }
            @Override public String getNumeroConsecutivo() { return consecutivo; }
            @Override public javax.xml.datatype.XMLGregorianCalendar getFechaEmision() { return cal2023(); }
            @Override public Emisor getEmisor() { return e; }
            @Override public Receptor getReceptor() { return r; }
            @Override public Resumen getResumenFactura() { return res; }
            @Override public DetalleServicio getDetalleServicio() { return Stream::empty; }
            @Override public Stream<InformacionReferencia> getInformacionReferencia() { return Stream.empty(); }
        };
    }

    private static FacturaExportacion facturaExportacion(String clave, String consecutivo) {
        Emisor e = emisor("Emisor FE", "77777", "01");
        Receptor r = receptor("Receptor FE", identificacion("88888", "02"));
        Resumen res = resumen("USD", new BigDecimal("600"), BigDecimal.ZERO, BigDecimal.TEN);
        return new FacturaExportacion() {
            @Override public String getClave() { return clave; }
            @Override public String getNumeroConsecutivo() { return consecutivo; }
            @Override public javax.xml.datatype.XMLGregorianCalendar getFechaEmision() { return cal2023(); }
            @Override public Emisor getEmisor() { return e; }
            @Override public Receptor getReceptor() { return r; }
            @Override public Resumen getResumenFactura() { return res; }
            @Override public DetalleServicio getDetalleServicio() { return Stream::empty; }
            @Override public Stream<InformacionReferencia> getInformacionReferencia() { return Stream.empty(); }
        };
    }

    private static MensajeHacienda mensajeHacienda(String clave, String emisorNum, BigDecimal impuesto, BigDecimal total) {
        return new MensajeHacienda() {
            @Override public String getClave() { return clave; }
            @Override public String getNumeroCedulaEmisor() { return emisorNum; }
            @Override public BigDecimal getMontoTotalImpuesto() { return impuesto; }
            @Override public BigDecimal getTotalFactura() { return total; }
        };
    }

    private static MensajeReceptor mensajeReceptor(String clave, String emisorNum, String receptorNum,
                                                    XMLGregorianCalendar fecha, BigDecimal impuesto, BigDecimal total) {
        return new MensajeReceptor() {
            @Override public String getClave() { return clave; }
            @Override public javax.xml.datatype.XMLGregorianCalendar getFechaEmisionDoc() { return fecha; }
            @Override public String getNumeroCedulaEmisor() { return emisorNum; }
            @Override public String getNumeroCedulaReceptor() { return receptorNum; }
            @Override public BigDecimal getMontoTotalImpuesto() { return impuesto; }
            @Override public BigDecimal getTotalFactura() { return total; }
        };
    }

    // ---- BaseMapper default methods ----

    @Test
    void baseMapper_getDefaultString_returnsNull() {
        assertNull(emisorMapper.getDefaultString());
    }

    @Test
    void baseMapper_getDefaultDate_returnsNull() {
        assertNull(emisorMapper.getDefaultDate());
    }

    @Test
    void baseMapper_getDefaultNumber_returnsNull() {
        assertNull(emisorMapper.getDefaultNumber());
    }

    // ---- EmisorMapper ----

    @Test
    void emisorMapper_of_empty_setsNombreNumeroTipo() {
        Emisor e = emisor("Acme Corp", "123456789", "01");
        name.julatec.ekonomi.tribunet.storage.Emisor result = emisorMapper.of(Optional.empty(), e);
        assertEquals("Acme Corp", result.getNombre());
        assertEquals("123456789", result.getNumero());
        assertEquals("01", result.getTipo());
    }

    @Test
    void emisorMapper_of_existingTarget_updatesFields() {
        name.julatec.ekonomi.tribunet.storage.Emisor existing = new name.julatec.ekonomi.tribunet.storage.Emisor();
        Emisor e = emisor("New Name", "999", "02");
        name.julatec.ekonomi.tribunet.storage.Emisor result = emisorMapper.of(Optional.of(existing), e);
        assertSame(existing, result);
        assertEquals("New Name", result.getNombre());
    }

    // ---- ReceptorMapper ----

    @Test
    void receptorMapper_of_empty_withIdentificacion_setsFields() {
        Receptor r = receptor("Receptor Co", identificacion("987654321", "02"));
        name.julatec.ekonomi.tribunet.storage.Receptor result = receptorMapper.of(Optional.empty(), r);
        assertEquals("Receptor Co", result.getNombre());
        assertEquals("987654321", result.getNumero());
        assertEquals("02", result.getTipo());
    }

    @Test
    void receptorMapper_of_nullIdentificacion_setsNullNumeroTipo() {
        Receptor r = receptor("Anonymous", null);
        name.julatec.ekonomi.tribunet.storage.Receptor result = receptorMapper.of(Optional.empty(), r);
        assertEquals("Anonymous", result.getNombre());
        assertNull(result.getNumero());
        assertNull(result.getTipo());
    }

    // ---- ResumenMapper ----

    @Test
    void resumenMapper_of_empty_mapsTotalComprobanteAndImpuesto() {
        Resumen res = resumen("CRC", BigDecimal.ONE, new BigDecimal("13"), new BigDecimal("113"));
        name.julatec.ekonomi.tribunet.storage.Resumen result = resumenMapper.of(Optional.empty(), res);
        assertEquals(new BigDecimal("113"), result.getTotalComprobante());
        assertEquals(new BigDecimal("13"), result.getTotalImpuesto());
        assertEquals("CRC", result.getCodigoMoneda());
        assertNotNull(result.getLastModified());
    }

    @Test
    void resumenMapper_of_empty_mapsCodigoMonedaAndTipoCambio() {
        Resumen res = resumen("USD", new BigDecimal("560"), BigDecimal.ZERO, BigDecimal.TEN);
        name.julatec.ekonomi.tribunet.storage.Resumen result = resumenMapper.of(Optional.empty(), res);
        assertEquals("USD", result.getCodigoMoneda());
        assertEquals(new BigDecimal("560"), result.getTipoCambio());
    }

    // ---- DocumentoMapper ----

    @Test
    void documentoMapper_of_empty_setsEmisorReceptorResumenFecha() {
        Factura f = factura("CLAVE001", "CONSEC001");
        name.julatec.ekonomi.tribunet.storage.Documento result = documentoMapper.of(Optional.empty(), f);
        assertNotNull(result.getEmisor());
        assertNotNull(result.getReceptor());
        assertNotNull(result.getResumen());
        assertNotNull(result.getFechaEmision());
        assertEquals("Test Emisor", result.getEmisor().getNombre());
        assertEquals("Test Receptor", result.getReceptor().getNombre());
    }

    // ---- FacturaMapper ----

    @Test
    void facturaMapper_of_empty_setsClaveConsecutivo() {
        Factura f = factura("CLAVE-F001", "CONSEC-F001");
        name.julatec.ekonomi.tribunet.storage.Factura result = facturaMapper.of(Optional.empty(), f);
        assertEquals("CLAVE-F001", result.getClave());
        assertEquals("CONSEC-F001", result.getNumeroConsecutivo());
        assertNotNull(result.getDocumento());
    }

    @Test
    void facturaMapper_of_existingTarget_reusesFactura() {
        name.julatec.ekonomi.tribunet.storage.Factura existing = new name.julatec.ekonomi.tribunet.storage.Factura();
        Factura f = factura("CLAVE-F002", "CONSEC-F002");
        name.julatec.ekonomi.tribunet.storage.Factura result = facturaMapper.of(Optional.of(existing), f);
        assertSame(existing, result);
        assertEquals("CLAVE-F002", result.getClave());
    }

    // ---- NotaCreditoMapper ----

    @Test
    void notaCreditoMapper_of_empty_setsClaveNota() {
        NotaCredito nc = notaCredito("CLAVE-NC001", "CONSEC-NC001");
        name.julatec.ekonomi.tribunet.storage.NotaCredito result = notaCreditoMapper.of(Optional.empty(), nc);
        assertNotNull(result.getClaveNota());
        assertEquals("CLAVE-NC001", result.getClaveNota().getClave());
        assertEquals("CONSEC-NC001", result.getClaveNota().getNumeroConsecutivo());
        assertNotNull(result.getDocumento());
    }

    // ---- NotaDebitoMapper ----

    @Test
    void notaDebitoMapper_of_empty_setsClaveNota() {
        NotaDebito nd = notaDebito("CLAVE-ND001", "CONSEC-ND001");
        name.julatec.ekonomi.tribunet.storage.NotaDebito result = notaDebitoMapper.of(Optional.empty(), nd);
        assertNotNull(result.getClaveNota());
        assertEquals("CLAVE-ND001", result.getClaveNota().getClave());
        assertEquals("CONSEC-ND001", result.getClaveNota().getNumeroConsecutivo());
        assertNotNull(result.getDocumento());
    }

    // ---- FacturaCompraMapper ----

    @Test
    void facturaCompraMapper_of_empty_setsClaveConsecutivo() {
        FacturaCompra fc = facturaCompra("CLAVE-FC001", "CONSEC-FC001");
        name.julatec.ekonomi.tribunet.storage.FacturaCompra result = facturaCompraMapper.of(Optional.empty(), fc);
        assertEquals("CLAVE-FC001", result.getClave());
        assertEquals("CONSEC-FC001", result.getNumeroConsecutivo());
        assertNotNull(result.getDocumento());
    }

    // ---- FacturaExportacionMapper ----

    @Test
    void facturaExportacionMapper_of_empty_setsClaveConsecutivo() {
        FacturaExportacion fe = facturaExportacion("CLAVE-FE001", "CONSEC-FE001");
        name.julatec.ekonomi.tribunet.storage.FacturaExportacion result = facturaExportacionMapper.of(Optional.empty(), fe);
        assertEquals("CLAVE-FE001", result.getClave());
        assertEquals("CONSEC-FE001", result.getNumeroConsecutivo());
        assertNotNull(result.getDocumento());
    }

    // ---- MensajeHaciendaMapper ----

    @Test
    void mensajeHaciendaMapper_of_empty_setsClaveTotalesEmisor() {
        MensajeHacienda mh = mensajeHacienda("CLAVE-MH001", "12345", new BigDecimal("13"), new BigDecimal("113"));
        Mensaje result = mensajeHaciendaMapper.of(Optional.empty(), mh);
        assertEquals("CLAVE-MH001", result.getClave());
        assertEquals("12345", result.getEmisorNumero());
        assertEquals(new BigDecimal("13"), result.getTotalImpuesto());
        assertEquals(new BigDecimal("113"), result.getTotalComprobante());
        assertNull(result.getReceptorNumero()); // not set by MensajeHaciendaMapper
    }

    @Test
    void mensajeHaciendaMapper_of_existingTarget_reusesTarget() {
        Mensaje existing = new Mensaje();
        MensajeHacienda mh = mensajeHacienda("CLAVE-MH002", "99999", BigDecimal.ZERO, BigDecimal.TEN);
        Mensaje result = mensajeHaciendaMapper.of(Optional.of(existing), mh);
        assertSame(existing, result);
        assertEquals("CLAVE-MH002", result.getClave());
    }

    // ---- MensajeReceptorMapper ----

    @Test
    void mensajeReceptorMapper_of_empty_setsAllFields() {
        MensajeReceptor mr = mensajeReceptor("CLAVE-MR001", "11111", "22222",
                cal2023(), new BigDecimal("5"), new BigDecimal("105"));
        Mensaje result = mensajeReceptorMapper.of(Optional.empty(), mr);
        assertEquals("CLAVE-MR001", result.getClave());
        assertEquals("11111", result.getEmisorNumero());
        assertEquals("22222", result.getReceptorNumero());
        assertNotNull(result.getReceptorFecha());
        assertEquals(new BigDecimal("5"), result.getTotalImpuesto());
        assertEquals(new BigDecimal("105"), result.getTotalComprobante());
    }

    @Test
    void mensajeReceptorMapper_of_nullFecha_receptorFechaIsNull() {
        MensajeReceptor mr = mensajeReceptor("CLAVE-MR002", "33333", "44444",
                null, BigDecimal.ZERO, BigDecimal.TEN);
        Mensaje result = mensajeReceptorMapper.of(Optional.empty(), mr);
        assertNull(result.getReceptorFecha());
    }
}

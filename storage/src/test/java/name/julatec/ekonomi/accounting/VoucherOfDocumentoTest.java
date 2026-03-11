package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.tribunet.*;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Voucher.of(Documento) static factory method.
 */
class VoucherOfDocumentoTest {

    private static XMLGregorianCalendar cal2023() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(2023, 6, 15, 10, 0, 0, 0, 0);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

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

    private static Receptor receptor(String nombre, String numero, String tipo) {
        return new Receptor() {
            @Override public String getNombre() { return nombre; }
            @Override public Identificacion getIdentificacion() { return identificacion(numero, tipo); }
        };
    }

    private static Resumen resumenCRC(BigDecimal total, BigDecimal impuesto) {
        return new Resumen() {
            @Override public String getCodigoMoneda() { return "CRC"; }
            @Override public BigDecimal getTipoCambio() { return null; }
            @Override public BigDecimal getTotalImpuesto() { return impuesto; }
            @Override public BigDecimal getTotalComprobante() { return total; }
        };
    }

    private static ImpuestoType impuestoExcento() {
        return new ImpuestoType() {
            @Override public ExoneracionType getExoneracion() { return new ExoneracionType() {}; }
            @Override public BigDecimal getMonto() { return BigDecimal.ZERO; }
            @Override public BigDecimal getTarifa() { return BigDecimal.ZERO; }
            @Override public String getCodigo() { return "01"; }
        };
    }

    private static LineaDetalle lineaSimple(BigDecimal amount) {
        return new LineaDetalle() {
            @Override public BigDecimal getMontoTotal() { return amount; }
            @Override public BigDecimal getSubTotal() { return amount; }
            @Override public Stream<ImpuestoType> getImpuesto() { return Stream.of(impuestoExcento()); }
            @Override public BigDecimal getMontoTotalLinea() { return amount; }
        };
    }

    private static Documento docWith(
            String clave, String consecutivo, Emisor e, Receptor r, Resumen res, BigDecimal lineAmount) {
        LineaDetalle linea = lineaSimple(lineAmount);
        DetalleServicio detalle = () -> Stream.of(linea);
        return new Documento() {
            @Override public String getClave() { return clave; }
            @Override public String getNumeroConsecutivo() { return consecutivo; }
            @Override public XMLGregorianCalendar getFechaEmision() { return cal2023(); }
            @Override public Emisor getEmisor() { return e; }
            @Override public Receptor getReceptor() { return r; }
            @Override public Resumen getResumenFactura() { return res; }
            @Override public DetalleServicio getDetalleServicio() { return detalle; }
            @Override public Stream<InformacionReferencia> getInformacionReferencia() { return Stream.empty(); }
        };
    }

    @Test
    void of_documento_mapsClaveAndConsecutivo() {
        Documento doc = docWith(
                "CLAVE-V001", "CONSEC-V001",
                emisor("Emisor SA", "123456789", "01"),
                receptor("Receptor SA", "987654321", "02"),
                resumenCRC(new BigDecimal("100"), BigDecimal.ZERO),
                new BigDecimal("100"));
        Voucher v = Voucher.of(doc);
        assertEquals("CLAVE-V001", v.getClave());
        assertEquals("CONSEC-V001", v.getConsecutivo());
    }

    @Test
    void of_documento_mapsFechaAndEmitter() {
        Documento doc = docWith(
                "CLAVE-V002", "CONSEC-V002",
                emisor("Acme Corp", "111111111", "01"),
                receptor("Client Corp", "222222222", "02"),
                resumenCRC(new BigDecimal("200"), BigDecimal.ZERO),
                new BigDecimal("200"));
        Voucher v = Voucher.of(doc);
        assertNotNull(v.getFecha());
        assertEquals("111111111", v.getEmisorNumero());
        assertEquals("Acme Corp", v.getEmisorNombre());
        assertEquals("222222222", v.getReceptorNumero());
        assertEquals("Client Corp", v.getReceptorNombre());
    }

    @Test
    void of_documento_mapsTotalComprobante() {
        Documento doc = docWith(
                "CLAVE-V003", "CONSEC-V003",
                emisor("E Corp", "333333333", "01"),
                receptor("R Corp", "444444444", "02"),
                resumenCRC(new BigDecimal("500"), BigDecimal.ZERO),
                new BigDecimal("500"));
        Voucher v = Voucher.of(doc);
        assertNotNull(v.getTotalComprobante());
        assertTrue(v.getTotalComprobante().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void of_documento_currencyIsCRC() {
        Documento doc = docWith(
                "CLAVE-V004", "CONSEC-V004",
                emisor("E", "555", "01"),
                receptor("R", "666", "02"),
                resumenCRC(new BigDecimal("100"), BigDecimal.ZERO),
                new BigDecimal("100"));
        Voucher v = Voucher.of(doc);
        assertEquals(Currency.getInstance("CRC"), v.getCurrency());
    }

    @Test
    void of_documento_withImpuesto13_setsF13() {
        ImpuestoType impuesto13 = new ImpuestoType() {
            @Override public ExoneracionType getExoneracion() { return new ExoneracionType() {}; }
            @Override public BigDecimal getMonto() { return new BigDecimal("13"); }
            @Override public BigDecimal getTarifa() { return new BigDecimal("13"); }
            @Override public String getCodigo() { return "01"; }
        };
        LineaDetalle linea = new LineaDetalle() {
            @Override public BigDecimal getMontoTotal() { return new BigDecimal("100"); }
            @Override public BigDecimal getSubTotal() { return new BigDecimal("100"); }
            @Override public Stream<ImpuestoType> getImpuesto() { return Stream.of(impuesto13); }
            @Override public BigDecimal getMontoTotalLinea() { return new BigDecimal("113"); }
        };
        DetalleServicio detalle = () -> Stream.of(linea);
        Resumen res = resumenCRC(new BigDecimal("113"), new BigDecimal("13"));
        Documento doc = new Documento() {
            @Override public String getClave() { return "CLAVE-F13"; }
            @Override public String getNumeroConsecutivo() { return "CONSEC-F13"; }
            @Override public XMLGregorianCalendar getFechaEmision() { return cal2023(); }
            @Override public Emisor getEmisor() { return emisor("E13", "777", "01"); }
            @Override public Receptor getReceptor() { return receptor("R13", "888", "02"); }
            @Override public Resumen getResumenFactura() { return res; }
            @Override public DetalleServicio getDetalleServicio() { return detalle; }
            @Override public Stream<InformacionReferencia> getInformacionReferencia() { return Stream.empty(); }
        };
        Voucher v = Voucher.of(doc);
        assertNotNull(v.getTotalF13());
        assertTrue(v.getTotalF13().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void of_notaCredito_negatesComprobante() {
        // NotaCredito uses preserve=false → values are negated
        ImpuestoType impuesto = new ImpuestoType() {
            @Override public ExoneracionType getExoneracion() { return new ExoneracionType() {}; }
            @Override public BigDecimal getMonto() { return BigDecimal.ZERO; }
            @Override public BigDecimal getTarifa() { return BigDecimal.ZERO; }
            @Override public String getCodigo() { return "01"; }
        };
        LineaDetalle linea = new LineaDetalle() {
            @Override public BigDecimal getMontoTotal() { return new BigDecimal("100"); }
            @Override public BigDecimal getSubTotal() { return new BigDecimal("100"); }
            @Override public Stream<ImpuestoType> getImpuesto() { return Stream.of(impuesto); }
            @Override public BigDecimal getMontoTotalLinea() { return new BigDecimal("100"); }
        };
        DetalleServicio detalle = () -> Stream.of(linea);
        Resumen res = resumenCRC(new BigDecimal("100"), BigDecimal.ZERO);
        NotaCredito nc = new NotaCredito() {
            @Override public String getClave() { return "NC-CLAVE"; }
            @Override public String getNumeroConsecutivo() { return "NC-001"; }
            @Override public XMLGregorianCalendar getFechaEmision() { return cal2023(); }
            @Override public Emisor getEmisor() { return emisor("E-NC", "999", "01"); }
            @Override public Receptor getReceptor() { return receptor("R-NC", "000", "02"); }
            @Override public Resumen getResumenFactura() { return res; }
            @Override public DetalleServicio getDetalleServicio() { return detalle; }
            @Override public Stream<InformacionReferencia> getInformacionReferencia() { return Stream.empty(); }
        };
        Voucher v = Voucher.of(nc);
        // preserve=false → values should be negated (negative total)
        assertTrue(v.getTotalComprobante().compareTo(BigDecimal.ZERO) < 0);
    }
}

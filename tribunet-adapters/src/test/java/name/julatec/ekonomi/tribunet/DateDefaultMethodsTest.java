package name.julatec.ekonomi.tribunet;

import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for date-related default methods in adapter interfaces.
 */
class DateDefaultMethodsTest {

    private static XMLGregorianCalendar cal2023() throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(
                2023, 1, 5, 10, 30, 0, 0, 0);
    }

    // ---- Documento.getFechaEmisionAsDate() ----

    @Test
    void documento_getFechaEmisionAsDate_returnsDate() throws DatatypeConfigurationException {
        XMLGregorianCalendar cal = cal2023();
        Documento doc = new Documento() {
            @Override public String getClave() { return null; }
            @Override public String getNumeroConsecutivo() { return null; }
            @Override public XMLGregorianCalendar getFechaEmision() { return cal; }
            @Override public Emisor getEmisor() { return null; }
            @Override public Receptor getReceptor() { return null; }
            @Override public Resumen getResumenFactura() { return null; }
            @Override public DetalleServicio getDetalleServicio() { return null; }
            @Override public Stream<InformacionReferencia> getInformacionReferencia() { return Stream.empty(); }
        };
        assertNotNull(doc.getFechaEmisionAsDate());
    }

    // ---- FacturaExportacion.getFechaEmisionAsDate() ----

    @Test
    void facturaExportacion_getFechaEmisionAsDate_returnsDate() throws DatatypeConfigurationException {
        XMLGregorianCalendar cal = cal2023();
        FacturaExportacion fe = new FacturaExportacion() {
            @Override public String getClave() { return null; }
            @Override public String getNumeroConsecutivo() { return null; }
            @Override public Emisor getEmisor() { return null; }
            @Override public Receptor getReceptor() { return null; }
            @Override public Resumen getResumenFactura() { return null; }
            @Override public DetalleServicio getDetalleServicio() { return null; }
            @Override public XMLGregorianCalendar getFechaEmision() { return cal; }
            @Override public Stream<InformacionReferencia> getInformacionReferencia() { return Stream.empty(); }
        };
        assertNotNull(fe.getFechaEmisionAsDate());
    }

    // ---- InformacionReferencia.getFechaEmisionAsDate() ----

    @Test
    void informacionReferencia_getFechaEmisionAsDate_returnsDate() throws DatatypeConfigurationException {
        XMLGregorianCalendar cal = cal2023();
        InformacionReferencia ref = new InformacionReferencia() {
            @Override public String getTipoDoc() { return "01"; }
            @Override public String getNumero() { return "ABC"; }
            @Override public XMLGregorianCalendar getFechaEmision() { return cal; }
            @Override public String getCodigo() { return null; }
            @Override public String getRazon() { return null; }
        };
        assertNotNull(ref.getFechaEmisionAsDate());
    }
}

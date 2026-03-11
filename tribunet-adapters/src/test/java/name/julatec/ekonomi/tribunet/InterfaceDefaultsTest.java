package name.julatec.ekonomi.tribunet;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for default methods on adapter interfaces.
 */
class InterfaceDefaultsTest {

    // ---- Resumen default methods ----

    @Test
    void resumen_getCodigoMoneda_returnsNull() {
        assertNull(minimal().getCodigoMoneda());
    }

    @Test
    void resumen_getTipoCambio_returnsNull() {
        assertNull(minimal().getTipoCambio());
    }

    @Test
    void resumen_getCodigoTipoMoneda_returnsThis() {
        Resumen r = minimal();
        assertSame(r, r.getCodigoTipoMoneda());
    }

    @Test
    void resumen_getTotalServGravados_returnsNull() {
        assertNull(minimal().getTotalServGravados());
    }

    @Test
    void resumen_getTotalServExentos_returnsNull() {
        assertNull(minimal().getTotalServExentos());
    }

    @Test
    void resumen_getTotalMercanciasGravadas_returnsNull() {
        assertNull(minimal().getTotalMercanciasGravadas());
    }

    @Test
    void resumen_getTotalMercanciasExentas_returnsNull() {
        assertNull(minimal().getTotalMercanciasExentas());
    }

    @Test
    void resumen_getTotalGravado_returnsNull() {
        assertNull(minimal().getTotalGravado());
    }

    @Test
    void resumen_getTotalExento_returnsNull() {
        assertNull(minimal().getTotalExento());
    }

    @Test
    void resumen_getTotalVenta_returnsNull() {
        assertNull(minimal().getTotalVenta());
    }

    @Test
    void resumen_getTotalDescuentos_returnsNull() {
        assertNull(minimal().getTotalDescuentos());
    }

    @Test
    void resumen_getTotalVentaNeta_returnsNull() {
        assertNull(minimal().getTotalVentaNeta());
    }

    @Test
    void resumen_getTotalIVADevuelto_returnsNull() {
        assertNull(minimal().getTotalIVADevuelto());
    }

    @Test
    void resumen_getTotalOtrosCargos_returnsNull() {
        assertNull(minimal().getTotalOtrosCargos());
    }

    // ---- ExoneracionType default methods ----

    @Test
    void exoneracion_getTipoDocumento_returnsNull() {
        assertNull(emptyExoneracion().getTipoDocumento());
    }

    @Test
    void exoneracion_getNumeroDocumento_returnsNull() {
        assertNull(emptyExoneracion().getNumeroDocumento());
    }

    @Test
    void exoneracion_getNombreInstitucion_returnsNull() {
        assertNull(emptyExoneracion().getNombreInstitucion());
    }

    @Test
    void exoneracion_getFechaEmision_returnsNull() {
        assertNull(emptyExoneracion().getFechaEmision());
    }

    @Test
    void exoneracion_getPorcentajeExoneracion_returnsNull() {
        assertNull(emptyExoneracion().getPorcentajeExoneracion());
    }

    @Test
    void exoneracion_getMontoExoneracion_returnsNull() {
        assertNull(emptyExoneracion().getMontoExoneracion());
    }

    // ---- ImpuestoType default methods ----

    @Test
    void impuestoType_getCodigo_returnsNull() {
        assertNull(emptyImpuesto().getCodigo());
    }

    @Test
    void impuestoType_getCodigoTarifa_returnsNull() {
        assertNull(emptyImpuesto().getCodigoTarifa());
    }

    @Test
    void impuestoType_getTarifa_returnsNull() {
        assertNull(emptyImpuesto().getTarifa());
    }

    @Test
    void impuestoType_getFactorIVA_returnsNull() {
        assertNull(emptyImpuesto().getFactorIVA());
    }

    @Test
    void impuestoType_getMonto_returnsNull() {
        assertNull(emptyImpuesto().getMonto());
    }

    @Test
    void impuestoType_getMontoExportacion_returnsNull() {
        assertNull(emptyImpuesto().getMontoExportacion());
    }

    @Test
    void impuestoType_getExoneracion_returnsNull() {
        assertNull(emptyImpuesto().getExoneracion());
    }

    // ---- InformacionReferencia default methods ----

    @Test
    void informacionReferencia_getTipoDocumento_usesFromCode() {
        InformacionReferencia ref = new InformacionReferencia() {
            @Override public String getTipoDoc() { return "01"; }
            @Override public String getNumero() { return null; }
            @Override public javax.xml.datatype.XMLGregorianCalendar getFechaEmision() { return null; }
            @Override public String getCodigo() { return null; }
            @Override public String getRazon() { return null; }
        };
        assertEquals(TipoDocumento.FACTURA, ref.getTipoDocumento());
    }

    @Test
    void informacionReferencia_getTipoDocumento_unknownCode_returnsDesconocido() {
        InformacionReferencia ref = new InformacionReferencia() {
            @Override public String getTipoDoc() { return "XX"; }
            @Override public String getNumero() { return null; }
            @Override public javax.xml.datatype.XMLGregorianCalendar getFechaEmision() { return null; }
            @Override public String getCodigo() { return null; }
            @Override public String getRazon() { return null; }
        };
        assertEquals(TipoDocumento.DESCONOCIDO, ref.getTipoDocumento());
    }

    // ---- MensajeReceptor default method ----

    @Test
    void mensajeReceptor_getFechaEmisionDocAsDate_nullCalendar_returnsNull() {
        MensajeReceptor m = new MensajeReceptor() {
            @Override public String getClave() { return null; }
            @Override public javax.xml.datatype.XMLGregorianCalendar getFechaEmisionDoc() { return null; }
            @Override public String getNumeroCedulaEmisor() { return null; }
            @Override public String getNumeroCedulaReceptor() { return null; }
            @Override public BigDecimal getMontoTotalImpuesto() { return null; }
            @Override public BigDecimal getTotalFactura() { return null; }
        };
        assertNull(m.getFechaEmisionDocAsDate());
    }

    // ---- LineaDetalle default methods ----

    @Test
    void lineaDetalle_getBaseImponible_returnsNull() {
        assertNull(minimalLineaDetalle().getBaseImponible());
    }

    @Test
    void lineaDetalle_getImpuestoNeto_returnsNull() {
        assertNull(minimalLineaDetalle().getImpuestoNeto());
    }

    // ---- Helpers ----

    private static Resumen minimal() {
        return new Resumen() {
            @Override
            public BigDecimal getTotalImpuesto() {
                return BigDecimal.ZERO;
            }

            @Override
            public BigDecimal getTotalComprobante() {
                return BigDecimal.ZERO;
            }
        };
    }

    private static ExoneracionType emptyExoneracion() {
        return new ExoneracionType() {};
    }

    private static ImpuestoType emptyImpuesto() {
        return new ImpuestoType() {};
    }

    private static LineaDetalle minimalLineaDetalle() {
        return new LineaDetalle() {
            @Override public BigDecimal getMontoTotal() { return BigDecimal.ZERO; }
            @Override public BigDecimal getSubTotal() { return BigDecimal.ZERO; }
            @Override public Stream<ImpuestoType> getImpuesto() { return Stream.empty(); }
            @Override public BigDecimal getMontoTotalLinea() { return BigDecimal.ZERO; }
        };
    }
}

package name.julatec.ekonomi.tribunet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TipoDocumentoTest {

    @ParameterizedTest
    @CsvSource({
            "01, FACTURA",
            "02, NOTA_DEBITO",
            "03, NOTA_CREDITO",
            "04, TIQUETE",
            "05, CONFIRMACION_DE_ACEPTACION",
            "06, CONFIRMACION_DE_ACEPTACION_PARCIAL",
            "07, CONFIRMACION_DE_RECHAZO",
            "08, FACTURA_DE_COMPRA",
            "09, FACTURA_DE_EXPORTACION",
    })
    void fromCode_knownCodes(String code, String expectedName) {
        assertEquals(TipoDocumento.valueOf(expectedName), TipoDocumento.fromCode(code));
    }

    @Test
    void fromCode_unknownCode_returnsDesconocido() {
        assertEquals(TipoDocumento.DESCONOCIDO, TipoDocumento.fromCode("00"));
        assertEquals(TipoDocumento.DESCONOCIDO, TipoDocumento.fromCode("XX"));
        assertEquals(TipoDocumento.DESCONOCIDO, TipoDocumento.fromCode("99"));
    }

    @Test
    void factura_targetClass_isFactura() {
        assertEquals(Factura.class, TipoDocumento.FACTURA.targetClass);
    }

    @Test
    void notaDebito_targetClass_isNotaDebito() {
        assertEquals(NotaDebito.class, TipoDocumento.NOTA_DEBITO.targetClass);
    }

    @Test
    void notaCredito_targetClass_isNotaCredito() {
        assertEquals(NotaCredito.class, TipoDocumento.NOTA_CREDITO.targetClass);
    }

    @Test
    void facturaDeCompra_targetClass_isFacturaCompra() {
        assertEquals(FacturaCompra.class, TipoDocumento.FACTURA_DE_COMPRA.targetClass);
    }

    @Test
    void facturaDeExportacion_targetClass_isFacturaExportacion() {
        assertEquals(FacturaExportacion.class, TipoDocumento.FACTURA_DE_EXPORTACION.targetClass);
    }

    @Test
    void allValues_haveNonNullCode() {
        for (TipoDocumento tipo : TipoDocumento.values()) {
            assertNotNull(tipo.code);
        }
    }

    @Test
    void allValues_haveNonNullTargetClass() {
        for (TipoDocumento tipo : TipoDocumento.values()) {
            assertNotNull(tipo.targetClass);
        }
    }

    @Test
    void values_count() {
        assertEquals(10, TipoDocumento.values().length);
    }
}

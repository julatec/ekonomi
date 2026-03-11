package name.julatec.ekonomi.tribunet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ImpuestoTypeCodigoTest {

    @ParameterizedTest
    @CsvSource({
            "01, ValorAgregado",
            "02, SelectivoDeConsumo",
            "03, Combustivos",
            "04, BebidasAlcoholicas",
            "05, BebidasEnvasadas",
            "06, ProductosDeTabaco",
            "07, ValorAgregadoEspecial",
            "08, ValorAgregadoUsados",
            "12, Cemento",
            "99, Otros",
    })
    void of_knownCodes(String code, String expectedName) {
        assertEquals(ImpuestoType.Codigo.valueOf(expectedName), ImpuestoType.Codigo.of(code));
    }

    @Test
    void of_null_returnsEmpty() {
        assertEquals(ImpuestoType.Codigo.Empty, ImpuestoType.Codigo.of(null));
    }

    @Test
    void of_emptyString_returnsEmpty() {
        assertEquals(ImpuestoType.Codigo.Empty, ImpuestoType.Codigo.of(""));
    }

    @Test
    void of_unknownCode_returnsNull() {
        assertNull(ImpuestoType.Codigo.of("00"));
        assertNull(ImpuestoType.Codigo.of("XX"));
    }

    @Test
    void empty_hasEmptyCode() {
        assertFalse(ImpuestoType.Codigo.Empty.code.isPresent());
    }

    @Test
    void baseImponible_hasEmptyCode() {
        assertFalse(ImpuestoType.Codigo.BaseImponible.code.isPresent());
    }

    @Test
    void otrosCargos_hasEmptyCode() {
        assertFalse(ImpuestoType.Codigo.OtrosCargos.code.isPresent());
    }

    @Test
    void total_hasEmptyCode() {
        assertFalse(ImpuestoType.Codigo.Total.code.isPresent());
    }

    @Test
    void valorAgregado_hasCode_01() {
        assertEquals(Optional.of("01"), ImpuestoType.Codigo.ValorAgregado.code);
    }

    @Test
    void ordering_empty_lessThan_baseImponible() {
        assertTrue(ImpuestoType.Codigo.Empty.compareTo(ImpuestoType.Codigo.BaseImponible) < 0);
    }

    @Test
    void ordering_baseImponible_lessThan_otrosCargos() {
        assertTrue(ImpuestoType.Codigo.BaseImponible.compareTo(ImpuestoType.Codigo.OtrosCargos) < 0);
    }

    @Test
    void ordering_otrosCargos_lessThan_valorAgregado() {
        assertTrue(ImpuestoType.Codigo.OtrosCargos.compareTo(ImpuestoType.Codigo.ValorAgregado) < 0);
    }

    @Test
    void allValues_haveNonNullCode() {
        for (ImpuestoType.Codigo codigo : ImpuestoType.Codigo.values()) {
            assertNotNull(codigo.code);
        }
    }
}

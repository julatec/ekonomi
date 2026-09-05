package name.julatec.ekonomi.mcp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La conversión de parámetros de herramienta a criterios.
 * <p>
 * Todo llega como texto desde el cliente MCP, así que acá es donde se decide qué
 * es un error del que consulta y qué es una consulta válida.
 */
class FiltroComprobantesTest {

    private static FiltroComprobantes filtro(String desde, String hasta) {
        return FiltroComprobantes.de(null, null, null, null, desde, hasta, null, null, null);
    }

    @Test
    @DisplayName("una búsqueda sin ningún criterio se reconoce como vacía")
    void reconoceElFiltroVacio() {
        assertTrue(FiltroComprobantes.de(null, null, null, null, null, null, null, null, null).vacio());
        assertFalse(FiltroComprobantes.de(null, null, "3-101-000000", null, null, null, null, null, null).vacio());
    }

    @Test
    @DisplayName("`hasta` incluye todo el día pedido, no se corta a medianoche")
    void hastaIncluyeElDiaCompleto() {
        final Date hasta = filtro(null, "2026-03-31").hasta();

        final Date medianocheDelPrimero = Date.from(
                LocalDate.of(2026, 4, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        assertTrue(hasta.before(medianocheDelPrimero), "no debe alcanzar el día siguiente");

        final Date esaTarde = Date.from(
                LocalDate.of(2026, 3, 31).atTime(23, 59).atZone(ZoneId.systemDefault()).toInstant());
        assertTrue(hasta.after(esaTarde), "una factura de las 23:59 del 31 tiene que entrar");
    }

    @Test
    @DisplayName("una fecha mal escrita dice cómo se escribe")
    void fechaInvalidaExplicaElFormato() {
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> filtro("31/03/2026", null));
        assertTrue(error.getMessage().contains("AAAA-MM-DD"));
        assertTrue(error.getMessage().contains("31/03/2026"), "conviene devolver el valor recibido");
    }

    @Test
    @DisplayName("un rango de fechas al revés se rechaza")
    void rangoInvertidoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> filtro("2026-06-01", "2026-01-01"));
    }

    @Test
    @DisplayName("un rango de montos al revés se rechaza")
    void montosInvertidosSeRechazan() {
        assertThrows(IllegalArgumentException.class,
                () -> FiltroComprobantes.de(null, null, null, null, null, null, "5000", "100", null));
    }

    @Test
    @DisplayName("un monto con separador de miles se rechaza en vez de truncarse")
    void montoConSeparadorSeRechaza() {
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> FiltroComprobantes.de(null, null, null, null, null, null, "1,500.00", null, null));
        assertTrue(error.getMessage().contains("separador de miles"));
    }

    @Test
    @DisplayName("los espacios sobrantes no cuentan como criterio")
    void losEspaciosNoSonCriterio() {
        assertTrue(FiltroComprobantes.de("  ", " ", "", null, null, null, null, null, "  ").vacio());
    }
}

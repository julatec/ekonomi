package name.julatec.ekonomi.preferencias;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Una preferencia guardada es una foto del catálogo del día que se guardó, y el catálogo
 * cambia: en agosto de 2026 el reporte pasó de 23 a 35 columnas al abrir las tarifas de la
 * v4.4 de Hacienda. Lo que se prueba acá es qué pasa con lo guardado cuando eso vuelva a
 * ocurrir — que es donde esto puede fallar callado.
 */
class ColumnasDeReporteTest {

    @Test
    @DisplayName("sin nada guardado, el reporte sale con el catálogo completo y en su orden")
    void sinPreferenciaDevuelveElCatalogoCompleto() {
        assertEquals(ColumnasDeReporte.catalogo(), ColumnasDeReporte.visibles(List.of()));
        assertEquals(ColumnasDeReporte.catalogo(), ColumnasDeReporte.visibles(null));
    }

    @Test
    @DisplayName("respeta el orden guardado y no escribe las apagadas")
    void respetaElOrdenYLaVisibilidad() {
        final List<ColumnaSeleccionada> guardadas = List.of(
                new ColumnaSeleccionada("Total Comprobante", true),
                new ColumnaSeleccionada("Clave", false),
                new ColumnaSeleccionada("Fecha", true));

        final List<String> visibles = ColumnasDeReporte.visibles(guardadas);

        assertEquals(List.of("Total Comprobante", "Fecha"), visibles.subList(0, 2));
        assertFalse(visibles.contains("Clave"));
    }

    @Test
    @DisplayName("una columna apagada conserva su lugar en el orden")
    void laApagadaNoPierdeSuLugar() {
        final List<ColumnaSeleccionada> guardadas = List.of(
                new ColumnaSeleccionada("Total Comprobante", true),
                new ColumnaSeleccionada("Clave", false),
                new ColumnaSeleccionada("Fecha", true));

        final List<ColumnaSeleccionada> reconciliadas = ColumnasDeReporte.reconciliar(guardadas);

        assertEquals("Clave", reconciliadas.get(1).getColumna());
        assertFalse(reconciliadas.get(1).isVisible());
    }

    @Test
    @DisplayName("una columna guardada que el reporte ya no tiene se descarta")
    void descartaLoQueYaNoEstaEnElCatalogo() {
        final List<ColumnaSeleccionada> guardadas = List.of(
                new ColumnaSeleccionada("Impuesto 1%", true),
                new ColumnaSeleccionada("Impuesto 13% (viejo)", true));

        final List<ColumnaSeleccionada> reconciliadas = ColumnasDeReporte.reconciliar(guardadas);

        assertTrue(reconciliadas.stream().noneMatch(c -> "Impuesto 13% (viejo)".equals(c.getColumna())));
        assertEquals("Impuesto 1%", reconciliadas.get(0).getColumna());
    }

    @Test
    @DisplayName("una columna nueva del reporte aparece al final y VISIBLE, no escondida")
    void laColumnaNuevaEntraVisible() {
        // Una preferencia vieja: solo las dos primeras columnas, guardadas antes de que el
        // reporte tuviera las demás. Todo lo que no menciona es "nuevo" desde su punto de
        // vista, que es exactamente lo que pasa cuando se agrega una tarifa.
        final List<ColumnaSeleccionada> guardadas = List.of(
                new ColumnaSeleccionada("Clave", true),
                new ColumnaSeleccionada("Fecha", false));

        final List<ColumnaSeleccionada> reconciliadas = ColumnasDeReporte.reconciliar(guardadas);
        final List<String> visibles = ColumnasDeReporte.visibles(guardadas);

        assertEquals(ColumnasDeReporte.catalogo().size(), reconciliadas.size(),
                "la reconciliación tiene que devolver el catálogo entero, apagadas incluidas");
        assertEquals("Clave", reconciliadas.get(0).getColumna());
        assertEquals("Fecha", reconciliadas.get(1).getColumna());
        for (String columna : ColumnasDeReporte.catalogo()) {
            if (!"Fecha".equals(columna)) {
                assertTrue(visibles.contains(columna),
                        "la columna «" + columna + "» no la apagó nadie y tiene que seguir saliendo");
            }
        }
        assertFalse(visibles.contains("Fecha"), "lo que sí se apagó sigue apagado");
    }

    @Test
    @DisplayName("una columna repetida en lo guardado se cuenta una sola vez")
    void colapsaLasRepetidas() {
        final List<ColumnaSeleccionada> guardadas = List.of(
                new ColumnaSeleccionada("Clave", true),
                new ColumnaSeleccionada("Clave", false));

        final List<ColumnaSeleccionada> reconciliadas = ColumnasDeReporte.reconciliar(guardadas);

        assertEquals(ColumnasDeReporte.catalogo().size(), reconciliadas.size());
        assertEquals(1, reconciliadas.stream().filter(c -> "Clave".equals(c.getColumna())).count());
        assertTrue(reconciliadas.get(0).isVisible(), "gana la primera aparición, no la última");
    }
}

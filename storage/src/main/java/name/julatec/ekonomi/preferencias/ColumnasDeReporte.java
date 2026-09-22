package name.julatec.ekonomi.preferencias;

import name.julatec.ekonomi.accounting.Voucher;
import name.julatec.ekonomi.report.csv.CsvBindByNameOrder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * El catálogo de columnas del reporte auxiliar y la reconciliación de lo que alguien guardó
 * contra ese catálogo.
 * <p>
 * El catálogo no se escribe acá: se lee de {@code @CsvBindByNameOrder} sobre {@link Voucher},
 * que ya es la fuente del orden con que sale el {@code .xlsx}. Una lista propia sería una
 * segunda verdad sobre lo mismo, y la que se olvidaría de actualizar. {@code VoucherTest}
 * comprueba que el encabezado que genera el libro coincide con esta lista.
 * <p>
 * <b>Lo que hace la reconciliación es evitar dos silencios.</b> Una preferencia guardada es una
 * foto del catálogo del día que se guardó, y el catálogo cambia —en agosto de 2026 se le
 * agregaron seis tarifas nuevas de la v4.4—. Entonces:
 * <ul>
 *   <li>una columna guardada que ya no está en el catálogo se descarta, en vez de pedirle al
 *       libro una columna que no existe;</li>
 *   <li>una columna del catálogo que la preferencia no menciona —porque es nueva— se agrega
 *       al final y <b>visible</b>. Ocultarla por omisión dejaría a quien tenga preferencia
 *       guardada sin una columna que sí existe y sin nada que se lo diga; y no mencionarla no
 *       es haberla apagado.</li>
 * </ul>
 */
public final class ColumnasDeReporte {

    /** El auxiliar de compras y ventas: {@code /report/sales} y {@code /report/purchases}. */
    public static final String REPORTE_COMPROBANTES = "comprobantes";

    private static final List<String> CATALOGO =
            List.of(Voucher.class.getAnnotation(CsvBindByNameOrder.class).value());

    private ColumnasDeReporte() {
    }

    /** Todas las columnas del reporte, en el orden por omisión. */
    public static List<String> catalogo() {
        return CATALOGO;
    }

    /** Lo guardado, sin lo que ya no existe y con lo que falta agregado al final. */
    public static List<ColumnaSeleccionada> reconciliar(List<ColumnaSeleccionada> guardadas) {
        final List<ColumnaSeleccionada> resultado = new ArrayList<>(CATALOGO.size());
        final Set<String> vistas = new HashSet<>();
        for (ColumnaSeleccionada guardada : guardadas == null ? List.<ColumnaSeleccionada>of() : guardadas) {
            if (guardada == null || guardada.getColumna() == null) {
                continue;
            }
            if (!CATALOGO.contains(guardada.getColumna()) || !vistas.add(guardada.getColumna())) {
                continue;
            }
            resultado.add(new ColumnaSeleccionada(guardada.getColumna(), guardada.isVisible()));
        }
        for (String columna : CATALOGO) {
            if (vistas.add(columna)) {
                resultado.add(new ColumnaSeleccionada(columna, true));
            }
        }
        return List.copyOf(resultado);
    }

    /**
     * Las columnas que van al libro, en orden.
     * <p>
     * Sin preferencia guardada devuelve el catálogo completo: el reporte sale exactamente
     * igual que antes de que esto existiera.
     */
    public static List<String> visibles(List<ColumnaSeleccionada> guardadas) {
        return reconciliar(guardadas).stream()
                .filter(ColumnaSeleccionada::isVisible)
                .map(ColumnaSeleccionada::getColumna)
                .toList();
    }
}

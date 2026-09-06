package name.julatec.ekonomi.session;

import name.julatec.util.algebraic.Interval;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * El rango de fechas por omisión (sin cookies "rangeLower"/"rangeUpper" todavía) tiene que
 * ser el mes en curso completo, no "los últimos 3 meses" ni "lo que va del mes".
 * <p>
 * Antes, {@code Workspace.getDefaultDateInterval()} devolvía [hoy - 3 meses, hoy]. Entrando
 * un día 6, "hasta" quedaba en ese mismo día 6: la pantalla de comprobantes se abría
 * mostrando 6 días del mes, no el mes. No se fija el día del mes en que corre esta prueba a
 * propósito —el rango tiene que dar el mes completo sin importar qué día sea hoy—.
 */
class RangoPorOmisionTest {

    private Calendar comoCalendar(Date fecha) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        return calendar;
    }

    @Test
    @DisplayName("el rango por omisión arranca el día 1 del mes en curso, a medianoche")
    void arrancaElDiaUnoDelMesEnCurso() {
        final Interval<Date> rango = Workspace.getDefaultDateInterval();
        final Calendar inicio = comoCalendar(rango.lower);
        final Calendar hoy = Calendar.getInstance();

        assertEquals(1, inicio.get(Calendar.DAY_OF_MONTH));
        assertEquals(hoy.get(Calendar.MONTH), inicio.get(Calendar.MONTH));
        assertEquals(hoy.get(Calendar.YEAR), inicio.get(Calendar.YEAR));
        assertEquals(0, inicio.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, inicio.get(Calendar.MINUTE));
        assertEquals(0, inicio.get(Calendar.SECOND));
    }

    @Test
    @DisplayName("el rango por omisión termina el último día del mes en curso, no hoy")
    void terminaElUltimoDiaDelMesEnCurso() {
        final Interval<Date> rango = Workspace.getDefaultDateInterval();
        final Calendar fin = comoCalendar(rango.upper);
        final Calendar hoy = Calendar.getInstance();

        assertEquals(hoy.getActualMaximum(Calendar.DAY_OF_MONTH), fin.get(Calendar.DAY_OF_MONTH));
        assertEquals(hoy.get(Calendar.MONTH), fin.get(Calendar.MONTH));
        assertEquals(hoy.get(Calendar.YEAR), fin.get(Calendar.YEAR));
        assertEquals(23, fin.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, fin.get(Calendar.MINUTE));
        assertEquals(59, fin.get(Calendar.SECOND));
    }
}

package name.julatec.ekonomi.session;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import name.julatec.ekonomi.security.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * El "hasta" que llega por cookie tiene que cubrir el día entero, no cortarse a medianoche.
 * <p>
 * {@code Workspace.getDateInterval()} lo consumen directo los reportes .xlsx
 * ({@code ReportController}) y el conteo de comprobantes por contraparte
 * ({@code ClienteController}) — a diferencia del buscador de comprobantes, que pasa por
 * {@code FiltroComprobantes.fechaFinal}, esto no tenía ningún ajuste—. Sin este arreglo,
 * elegir "hasta" el día de hoy en la barra superior dejaba fuera del reporte casi todo ese
 * día: la cookie se parsea a las 00:00:00, y un {@code BETWEEN}/{@code <=} contra esa hora
 * excluye cualquier comprobante emitido después.
 */
class RangoDesdeCookieTest {

    private static User usuarioCon(String... datasources) {
        final User user = mock(User.class);
        when(user.getDatasources()).thenReturn(new LinkedHashSet<>(Set.of(datasources)));
        when(user.getImportBankAccounts()).thenReturn(Set.of());
        when(user.getImportManualTransactions()).thenReturn(Set.of());
        when(user.getImportBankOperations()).thenReturn(Set.of());
        return user;
    }

    private static HttpServletRequest peticionConRango(String desde, String hasta) {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie(Workspace.Interval_LOWER_COOKIE, desde),
                new Cookie(Workspace.Interval_UPPER_COOKIE, hasta),
        });
        when(request.getLocale()).thenReturn(Locale.getDefault());
        return request;
    }

    @Test
    @DisplayName("el \"hasta\" de la cookie llega al final del día, no a medianoche")
    void hastaCubreElDiaEntero() {
        final Workspace workspace = new Workspace(usuarioCon("julatec"));

        workspace.setRequest(peticionConRango("2026-09-01", "2026-09-06"));

        final Calendar fin = Calendar.getInstance();
        fin.setTime(workspace.getDateInterval().upper);
        assertEquals(Calendar.SEPTEMBER, fin.get(Calendar.MONTH));
        assertEquals(6, fin.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, fin.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, fin.get(Calendar.MINUTE));
        assertEquals(59, fin.get(Calendar.SECOND));
    }

    @Test
    @DisplayName("el \"desde\" de la cookie se queda en medianoche, no se le suma nada")
    void desdeQuedaEnMedianoche() {
        final Workspace workspace = new Workspace(usuarioCon("julatec"));

        workspace.setRequest(peticionConRango("2026-09-01", "2026-09-06"));

        final Calendar inicio = Calendar.getInstance();
        inicio.setTime(workspace.getDateInterval().lower);
        assertEquals(1, inicio.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, inicio.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, inicio.get(Calendar.MINUTE));
    }
}

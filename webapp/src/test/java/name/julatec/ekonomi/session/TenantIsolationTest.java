package name.julatec.ekonomi.session;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import name.julatec.ekonomi.security.User;
import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Fija el aislamiento entre tenants.
 * <p>
 * El tenant se elige con una cookie, que la controla por completo quien hace la
 * petición. Mientras ese valor se aceptaba sin contrastarlo con los datasources
 * del usuario, editar la cookie bastaba para leer la contabilidad de otro.
 */
class TenantIsolationTest {

    private static final String PROPIO = "agropag";
    private static final String AJENO = "contabilidad-de-otro";

    @BeforeEach
    @AfterEach
    void limpiarTenant() {
        MultiTenantRepository.clear();
    }

    private static User usuarioCon(String... datasources) {
        final User user = mock(User.class);
        when(user.getDatasources()).thenReturn(new LinkedHashSet<>(Set.of(datasources)));
        when(user.getImportBankAccounts()).thenReturn(Set.of());
        when(user.getImportManualTransactions()).thenReturn(Set.of());
        when(user.getImportBankOperations()).thenReturn(Set.of());
        return user;
    }

    private static HttpServletRequest peticionConTenant(String tenant) {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(
                new Cookie[]{new Cookie(Workspace.TENANT_COOKIE, tenant)});
        when(request.getLocale()).thenReturn(Locale.getDefault());
        return request;
    }

    @Test
    @DisplayName("una cookie con un tenant ajeno cae al propio, no lo abre")
    void cookieConTenantAjenoNoAbreEsaContabilidad() {
        final Workspace workspace = new Workspace(usuarioCon(PROPIO));

        workspace.setRequest(peticionConTenant(AJENO));

        assertEquals(PROPIO, workspace.getTargetPersistanceUnit(),
                "el workspace no debe apuntar a un tenant que no es del usuario");
        assertEquals(PROPIO, MultiTenantRepository.getCurrentTenant(),
                "la conexión a base de datos tampoco debe apuntar ahí");
    }

    @Test
    @DisplayName("una cookie con un tenant propio sí se respeta")
    void cookieConTenantPropioSeRespeta() {
        final Workspace workspace = new Workspace(usuarioCon(PROPIO, "julatec"));

        workspace.setRequest(peticionConTenant("julatec"));

        assertEquals("julatec", workspace.getTargetPersistanceUnit());
        assertEquals("julatec", MultiTenantRepository.getCurrentTenant());
    }

    @Test
    @DisplayName("sin cookie se usa el primer datasource del usuario")
    void sinCookieSeUsaElPrimerDatasource() {
        final Workspace workspace = new Workspace(usuarioCon(PROPIO));
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);
        when(request.getLocale()).thenReturn(Locale.getDefault());

        workspace.setRequest(request);

        assertEquals(PROPIO, workspace.getTargetPersistanceUnit());
    }

    @Test
    @DisplayName("el filtro descarta el tenant al terminar la petición")
    void elFiltroDescartaElTenant() throws Exception {
        MultiTenantRepository.setCurrentDb(PROPIO);

        new TenantCleanupFilter().doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (request, response) -> assertEquals(
                        PROPIO, MultiTenantRepository.getCurrentTenant(),
                        "durante la petición el tenant sigue disponible"));

        assertNull(MultiTenantRepository.getCurrentTenant(),
                "al terminar debe quedar limpio: Tomcat reutiliza el hilo");
    }

    @Test
    @DisplayName("el filtro limpia incluso si la petición falla")
    void elFiltroLimpiaAunqueLaPeticionFalle() {
        MultiTenantRepository.setCurrentDb(PROPIO);

        assertThrows(IllegalStateException.class, () ->
                new TenantCleanupFilter().doFilter(
                        new MockHttpServletRequest(),
                        new MockHttpServletResponse(),
                        (request, response) -> {
                            throw new IllegalStateException("falla dentro de la petición");
                        }));

        assertNull(MultiTenantRepository.getCurrentTenant(),
                "una excepción no debe dejar el tenant pegado al hilo");
    }
}

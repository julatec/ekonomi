package name.julatec.ekonomi.mcp;

import name.julatec.ekonomi.security.User;
import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * El equivalente de {@code TenantIsolationTest} para la vía MCP.
 * <p>
 * En el webapp el tenant llega por cookie; acá llega como parámetro de la
 * herramienta. Es el mismo dato controlado por quien consulta, así que tiene que
 * cuidarse igual: solo vale si está entre los datasources del usuario del
 * certificado. Una herramienta que respondiera sobre otra contabilidad daría una
 * respuesta creíble y equivocada, que es peor que un error.
 */
class AccesoTenantTest {

    private static final String PROPIO = "agropag";
    private static final String AJENO = "contabilidad-de-otro";

    private final AccesoTenant acceso = new AccesoTenant();

    @BeforeEach
    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
        MultiTenantRepository.clear();
    }

    private static void autenticar(String... datasources) {
        final User user = mock(User.class);
        when(user.getDatasources()).thenReturn(new LinkedHashSet<>(Set.of(datasources)));
        when(user.getUsername()).thenReturn("prueba");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @Test
    @DisplayName("sin sesión autenticada no se abre ninguna contabilidad")
    void sinSesionNoHayAcceso() {
        assertThrows(IllegalStateException.class, () -> acceso.en(PROPIO, () -> "no debería llegar"));
    }

    @Test
    @DisplayName("un tenant ajeno se rechaza; no se cae al propio en silencio")
    void tenantAjenoSeRechaza() {
        autenticar(PROPIO);

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> acceso.en(AJENO, () -> "no debería llegar"));

        assertFalse(error.getMessage().contains(AJENO),
                "el mensaje no debe confirmar la existencia del tenant pedido");
        assertNull(MultiTenantRepository.getCurrentTenant(),
                "un intento fallido no debe dejar ninguna conexión apuntada");
    }

    @Test
    @DisplayName("un tenant propio sí se abre, y apunta ahí durante la acción")
    void tenantPropioSeAbre() {
        autenticar(PROPIO, "julatec");

        final String visto = acceso.en("julatec", MultiTenantRepository::getCurrentTenant);

        assertEquals("julatec", visto);
    }

    @Test
    @DisplayName("al terminar se restaura el tenant anterior del hilo")
    void seRestauraElTenantAnterior() {
        autenticar(PROPIO, "julatec");
        MultiTenantRepository.setCurrentDb(PROPIO);

        acceso.en("julatec", () -> "listo");

        assertEquals(PROPIO, MultiTenantRepository.getCurrentTenant(),
                "la herramienta no debe cambiarle el tenant al resto de la petición");
    }

    @Test
    @DisplayName("si la acción falla, el hilo igual queda limpio")
    void seLimpiaAunqueLaAccionFalle() {
        autenticar("julatec");

        assertThrows(IllegalStateException.class, () -> acceso.en("julatec", () -> {
            throw new IllegalStateException("falla dentro de la herramienta");
        }));

        assertNull(MultiTenantRepository.getCurrentTenant(),
                "Tomcat reutiliza los hilos: un tenant pegado se lo encuentra la próxima petición");
    }

    @Test
    @DisplayName("no indicar tenant es un error con la lista de los disponibles")
    void sinTenantSeListanLosDisponibles() {
        autenticar(PROPIO);

        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> acceso.en(" ", () -> "no debería llegar"));

        assertTrue(error.getMessage().contains(PROPIO),
                "el error debe decir qué contabilidades sí puede abrir");
    }
}

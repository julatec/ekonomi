package name.julatec.ekonomi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code autenticadoYNoEsKilla()} es la única barrera activa hoy para todo lo
 * que no sea /latex/**, /pitia/** ni /firma/**: method security está apagado
 * en producción (ver {@link MethodSecurityConfig}), así que un error acá no
 * lo tapa ninguna otra capa.
 * <p>
 * Por eso estas pruebas no son un puñado de ejemplos sueltos: cubren, por
 * exhaustividad, las cuatro particiones del espacio de entrada de las que
 * depende la función --nulidad, autenticación, anonimato, y pertenencia a
 * ROLE_KILLA-- más el caso de un rol distinto, para dejar constancia de que
 * ganar cualquier otro rol en el futuro (ROLE_USER, ROLE_ADMIN) no vuelve a
 * abrir lo que esto cierra.
 */
class EkonomiApplicationTest {

    private static final RequestAuthorizationContext CONTEXTO_IRRELEVANTE = null;

    private final AuthorizationManager<RequestAuthorizationContext> regla =
            EkonomiApplication.autenticadoYNoEsKilla();

    private Authentication autenticacionCon(boolean autenticado, GrantedAuthority... autoridades) {
        final Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(autenticado);
        when(auth.getAuthorities()).thenReturn((Set) Set.of(autoridades));
        return auth;
    }

    private boolean permite(Authentication auth) {
        final AuthorizationResult resultado = regla.authorize(() -> auth, CONTEXTO_IRRELEVANTE);
        return resultado != null && resultado.isGranted();
    }

    @Test
    @DisplayName("caso 1/6 -- sin Authentication (null): niega")
    void sinAutenticacion() {
        assertFalse(permite(null));
    }

    @Test
    @DisplayName("caso 2/6 -- Authentication presente pero no autenticada: niega")
    void noAutenticada() {
        assertFalse(permite(autenticacionCon(false)));
    }

    @Test
    @DisplayName("caso 3/6 -- anónima (AnonymousAuthenticationToken): niega")
    void anonima() {
        final Authentication anonima = new AnonymousAuthenticationToken(
                "clave", "anonimo", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        assertFalse(permite(anonima));
    }

    @Test
    @DisplayName("caso 4/6 -- autenticada, ROLE_KILLA: niega -- es la regla que existe para esto")
    void autenticadaComoKilla() {
        assertFalse(permite(autenticacionCon(true, new SimpleGrantedAuthority("ROLE_KILLA"))));
    }

    @Test
    @DisplayName("caso 5/6 -- autenticada, sin ningún rol: permite "
            + "(los dos usuarios reales de producción hoy, user_roles vacía)")
    void autenticadaSinRoles() {
        assertTrue(permite(autenticacionCon(true)));
    }

    @Test
    @DisplayName("caso 6/6 -- autenticada, con un rol que no es ROLE_KILLA: permite "
            + "(ganar ROLE_USER/ROLE_ADMIN el día de mañana no vuelve a abrir esto)")
    void autenticadaConOtroRol() {
        assertTrue(permite(autenticacionCon(true, new SimpleGrantedAuthority("ROLE_USER"))));
    }
}

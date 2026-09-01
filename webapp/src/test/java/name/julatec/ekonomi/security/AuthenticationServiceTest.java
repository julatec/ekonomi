package name.julatec.ekonomi.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Fija el cierre del bypass de autenticación.
 * <p>
 * Ekonomi es alcanzable desde internet y su repositorio es público. Mientras
 * {@code User.getPassword()} devolvía una constante y {@code loadUserByUsername}
 * resolvía usuarios por cédula, cualquiera podía entrar con dos datos públicos.
 * Estas pruebas existen para que eso no vuelva a compilar sin que alguien lo note.
 */
class AuthenticationServiceTest {

    private static final String ISSUER = "CA SINPE - PERSONA FISICA v2";
    private static final String CEDULA = "0503590732";

    private AuthenticationService serviceConUsuarioRegistrado() {
        final User user = mock(User.class);
        when(user.getIds()).thenReturn(Set.of(
                new UserId().setIssuer(ISSUER).setValue(CEDULA)));

        final UserRepository users = mock(UserRepository.class);
        final IssuerRepository issuers = mock(IssuerRepository.class);
        when(users.findAll()).thenReturn(List.of(user));
        when(issuers.findAll()).thenReturn(List.of(
                new Issuer().setName(ISSUER).setField("SERIALNUMBER")));

        return new AuthenticationService(users, issuers);
    }

    @Test
    @DisplayName("la cédula de un usuario registrado no basta para autenticarse")
    void laCedulaDeUnUsuarioRegistradoNoAutentica() {
        // Es el caso peligroso: el usuario existe y la cédula es correcta.
        // Aun así debe rechazarse, porque sin certificado no hay identidad probada.
        assertThrows(
                UsernameNotFoundException.class,
                () -> serviceConUsuarioRegistrado().loadUserByUsername(CEDULA));
    }

    @Test
    @DisplayName("una cédula desconocida tampoco autentica")
    void unaCedulaDesconocidaNoAutentica() {
        assertThrows(
                UsernameNotFoundException.class,
                () -> serviceConUsuarioRegistrado().loadUserByUsername("9999999999"));
    }

    @Test
    @DisplayName("sin certificado X.509 la autenticación previa falla")
    void sinCertificadoNoHayAutenticacionPrevia() {
        final PreAuthenticatedAuthenticationToken token =
                new PreAuthenticatedAuthenticationToken("principal", "no-soy-un-certificado");
        assertThrows(
                UsernameNotFoundException.class,
                () -> serviceConUsuarioRegistrado().loadUserDetails(token));
    }

    @Test
    @DisplayName("User no expone ninguna contraseña que un proveedor pueda validar")
    void elUsuarioNoTieneContrasena() {
        assertNull(
                new User().getPassword(),
                "getPassword() debe devolver null: devolver cualquier cadena reactiva "
                        + "la autenticación por contraseña para todos los usuarios");
    }
}

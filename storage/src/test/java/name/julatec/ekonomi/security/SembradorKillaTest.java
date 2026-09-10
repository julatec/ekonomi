package name.julatec.ekonomi.security;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Que el sembrador deje exactamente el Issuer y el User que
 * {@link AuthenticationService} necesita para resolver el certificado de
 * Killa -- no un {@code field} equivocado, ni un rol de más.
 */
class SembradorKillaTest {

    @Test
    void siembraElIssuerCorrecto() {
        final UserRepository users = mock(UserRepository.class);
        final IssuerRepository issuers = mock(IssuerRepository.class);

        new SembradorKilla(users, issuers).run(null);

        final ArgumentCaptor<Issuer> capturado = ArgumentCaptor.forClass(Issuer.class);
        verify(issuers).save(capturado.capture());
        assertEquals(SembradorKilla.ISSUER_NAME, capturado.getValue().getName());
        assertEquals(SembradorKilla.ISSUER_FIELD, capturado.getValue().getField());
    }

    @Test
    void siembraElUsuarioConSoloRoleKilla() {
        final UserRepository users = mock(UserRepository.class);
        final IssuerRepository issuers = mock(IssuerRepository.class);

        new SembradorKilla(users, issuers).run(null);

        final ArgumentCaptor<User> capturado = ArgumentCaptor.forClass(User.class);
        verify(users).save(capturado.capture());
        final User usuario = capturado.getValue();

        assertEquals(SembradorKilla.USERNAME, usuario.getUsername());
        assertEquals(Set.of(SembradorKilla.ROLE), usuario.getRoles());
        assertTrue(usuario.getDatasources().isEmpty());
    }

    @Test
    void elUserIdCoincideConLoQueTraeElCertificadoDeInti() {
        final UserRepository users = mock(UserRepository.class);
        final IssuerRepository issuers = mock(IssuerRepository.class);

        new SembradorKilla(users, issuers).run(null);

        final ArgumentCaptor<User> capturado = ArgumentCaptor.forClass(User.class);
        verify(users).save(capturado.capture());
        final UserId id = capturado.getValue().getIds().iterator().next();

        // Éste es el par exacto que AuthenticationService arma a partir del
        // certificado: CN del emisor -> Issuer.getField() ("CN") -> CN del
        // sujeto. Un valor distinto acá y el 401 vuelve, silencioso.
        assertEquals(SembradorKilla.ISSUER_NAME, id.getIssuer());
        assertEquals(SembradorKilla.USER_ID_VALUE, id.getValue());
        assertEquals(1, capturado.getValue().getIds().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void unFalloDeGuardadoNoTumbaElArranque() {
        // Pasó de verdad: la base en --read-only hizo que issuerRepository.save()
        // lanzara, y como nada lo atrapaba, tumbó el ApplicationContext entero
        // de killa, no sólo la siembra -- mismo motivo que core.telegram en
        // Inti nunca deja que un aviso fallido tumbe la operación que lo pidió.
        final UserRepository users = mock(UserRepository.class);
        final IssuerRepository issuers = mock(IssuerRepository.class);
        doThrow(new RuntimeException("--read-only")).when(issuers).save(org.mockito.ArgumentMatchers.any());

        assertDoesNotThrow(() -> new SembradorKilla(users, issuers).run(null));
    }
}

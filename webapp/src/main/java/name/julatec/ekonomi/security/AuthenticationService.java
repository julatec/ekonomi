package name.julatec.ekonomi.security;

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AuthenticationService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private final Map<String, Issuer> issuerMap;
    private final Map<UserId, User> userMap;

    public AuthenticationService(
            UserRepository userRepository,
            IssuerRepository issuerRepository) {
        this.issuerMap = new TreeMap<>();
        issuerRepository.findAll().forEach(issuer -> issuerMap.put(issuer.getName(), issuer));
        this.userMap = new TreeMap<>();
        userRepository.findAll().forEach(user -> user.getIds().forEach(userId -> userMap.put(userId, user)));
    }

    private Map<String, String> getNames(X500Principal principal) throws InvalidNameException {
        final LdapName ldapDN = new LdapName(String.valueOf(principal));
        final TreeMap<String, String> values = new TreeMap<>();
        for (Rdn rdn : ldapDN.getRdns()) {
            values.put(rdn.getType(), String.valueOf(rdn.getValue()));
        }
        return Collections.unmodifiableMap(values);
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken) throws UsernameNotFoundException {
        try {
            if (preAuthenticatedAuthenticationToken.getCredentials() instanceof X509Certificate) {
                final X509Certificate certificate = (X509Certificate) preAuthenticatedAuthenticationToken.getCredentials();
                certificate.checkValidity();
                final Map<String, String> providerNames = getNames(certificate.getIssuerX500Principal());
                final Map<String, String> userNames = getNames(certificate.getSubjectX500Principal());
                final String issuerName = providerNames.get("CN");
                final Issuer issuer = issuerMap.get(issuerName);
                if (issuer != null) {
                    final String userName = userNames.get(issuer.getField());
                    final UserId userId = new UserId().setIssuer(issuerName).setValue(userName);
                    if (userMap.containsKey(userId)) {
                        return userMap.get(userId);
                    }
                }
            }
        } catch (CertificateExpiredException e) {
            throw new UsernameNotFoundException("El certificado ya expiro.");
        } catch (CertificateNotYetValidException e) {
            throw new UsernameNotFoundException("El certificado aun no es valido.");
        } catch (InvalidNameException e) {
            throw new UsernameNotFoundException("Firma digital invalida.", e);
        }
        throw new UsernameNotFoundException("Se requiere autenticarse con firma digital.");
    }
}
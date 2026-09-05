package name.julatec.ekonomi.mcp;

import name.julatec.ekonomi.security.User;
import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * Decide contra qué contabilidad corre cada herramienta, y con qué derecho.
 * <p>
 * El tenant llega como parámetro de la herramienta, es decir: lo propone quien
 * hace la consulta. Eso lo pone en la misma categoría que la cookie {@code tenant}
 * del webapp, que durante un tiempo se aceptaba tal cual y bastaba editarla para
 * leer la contabilidad de otro. La regla es la misma que fijó aquel arreglo y que
 * cuida {@code TenantIsolationTest}: un tenant solo vale si está en
 * {@link User#getDatasources()} del usuario que presentó el certificado.
 * <p>
 * A diferencia del webapp, acá no se cae al primer datasource cuando el pedido no
 * es válido: una herramienta que silenciosamente responde sobre otra contabilidad
 * daría una respuesta creíble y equivocada. Se falla y se dice por qué.
 */
@Component
public class AccesoTenant {

    /** El usuario autenticado por certificado, o un fallo si no hay ninguno. */
    public User usuario() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "No hay sesión autenticada. Este servidor MCP exige certificado de firma digital.");
        }
        if (!(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException(
                    "La sesión no corresponde a un usuario de Ekonomi.");
        }
        return user;
    }

    /** Las contabilidades que el usuario autenticado puede abrir. */
    public Set<String> tenantsDelUsuario() {
        return new TreeSet<>(usuario().getDatasources());
    }

    private String validado(String tenant) {
        if (tenant == null || tenant.isBlank()) {
            throw new IllegalArgumentException(
                    "Falta indicar el tenant. Disponibles para este usuario: "
                            + String.join(", ", tenantsDelUsuario()));
        }
        final Set<String> propios = tenantsDelUsuario();
        if (!propios.contains(tenant)) {
            // No se nombra el tenant pedido en la respuesta: si no es suyo, que la
            // herramienta no sirva para confirmar que existe.
            throw new IllegalArgumentException(
                    "Ese tenant no pertenece a este usuario. Disponibles: "
                            + String.join(", ", propios));
        }
        return tenant;
    }

    /**
     * Corre la acción apuntando a la contabilidad indicada y restaura la anterior
     * al terminar, pase lo que pase: Tomcat reutiliza los hilos y un tenant pegado
     * al hilo se lo encuentra la siguiente petición.
     */
    public <R> R en(String tenant, Supplier<R> accion) {
        final String valido = validado(tenant);
        final String anterior = MultiTenantRepository.getCurrentTenant();
        try {
            MultiTenantRepository.setCurrentDb(valido);
            return accion.get();
        } finally {
            if (anterior == null) {
                MultiTenantRepository.clear();
            } else {
                MultiTenantRepository.setCurrentDb(anterior);
            }
        }
    }
}

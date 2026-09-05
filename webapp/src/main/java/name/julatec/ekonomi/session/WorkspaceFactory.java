package name.julatec.ekonomi.session;

import name.julatec.ekonomi.security.User;
import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * El workspace por usuario: qué contabilidad tiene abierta y con qué rango de fechas.
 * <p>
 * Ya no arma la lista de contrapartes. Antes cada login y cada cambio de tenant traía
 * {@code FacturaRepository.getClients()} —la tabla {@code factura} entera— para de-duplicarla
 * en memoria con un {@code Collector} propio, y el resultado viajaba dentro de la sesión
 * aunque la pantalla no lo estuviera mirando. Eso lo hace ahora {@code GET /api/clients}, que
 * pagina y filtra en la base y solo corre cuando alguien abre esa pantalla.
 */
@Service
public class WorkspaceFactory {

    final Map<String, Workspace> workspaceMap = new ConcurrentSkipListMap<>();

    private ApplicationContext applicationContext;

    synchronized Workspace getWorkspace(Authentication authentication, HttpServletRequest request) {

        final User user = (User) authentication.getPrincipal();

        if (workspaceMap.containsKey(user.getUsername())) {
            return workspaceMap.get(user.getUsername()).setRequest(request);
        }

        final Workspace workspace = applicationContext.getBean(Workspace.class, user);
        workspaceMap.put(user.getUsername(), workspace);

        if (MultiTenantRepository.getCurrentTenant() == null) {
            boolean selected = false;
            // getCookies() devuelve null cuando la petición no trae ninguna.
            final Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies == null ? new Cookie[0] : cookies) {
                switch (cookie.getName()) {
                    case Workspace.TENANT_COOKIE:
                        // La cookie es del cliente: solo se acepta si el tenant
                        // pertenece al usuario. Ver Workspace#getTenantFromCookie.
                        if (user.getDatasources().contains(cookie.getValue())) {
                            workspace.setTargetPersistanceUnit(cookie.getValue());
                            MultiTenantRepository.setCurrentDb(cookie.getValue());
                            selected = true;
                        }
                        break;
                    default:
                        break;
                }
                if (selected) {
                    break;
                }
            }
            if (!selected) {
                final String firstDb = user.getDatasources().iterator().next();
                MultiTenantRepository.setCurrentDb(firstDb);
                workspace.setTargetPersistanceUnit(firstDb);
            }
        }

        return workspaceMap.get(user.getUsername()).setRequest(request);
    }

    @Autowired
    public WorkspaceFactory setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }
}

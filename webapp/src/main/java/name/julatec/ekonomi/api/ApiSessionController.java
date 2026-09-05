package name.julatec.ekonomi.api;

import jakarta.servlet.http.HttpServletRequest;
import name.julatec.ekonomi.session.Session;
import name.julatec.ekonomi.session.Workspace;
import name.julatec.ekonomi.session.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * El arranque de la SPA, en JSON.
 * <p>
 * Reemplaza a {@code GET /js/session.js}, que devolvía <b>JavaScript literal</b>
 * ({@code const session = {...}; const app = {...};}) para que los componentes lo leyeran
 * como variables globales. Eso ataba el frontend a que lo sirviera esta aplicación y hacía
 * imposible consumirlo desde el dev server de Vite.
 * <p>
 * A diferencia de aquél, <b>no trae la lista de clientes</b>: se calculaba entera en cada
 * login y cada cambio de tenant con una consulta sin paginar sobre toda la tabla. Ahora vive
 * en {@code GET /api/clients}, que pagina y filtra.
 */
@RestController
@Secured({"ROLE_ADMIN", "ROLE_USER"})
public class ApiSessionController {

    private WorkspaceService workspaceService;

    public record SesionDto(
            String username,
            String tenant,
            Set<String> tenants,
            Date desde,
            Date hasta,
            Set<String> inboxes,
            Set<Session.ImportAccount> importAccounts,
            Map<String, Object> mensajes) {
    }

    @GetMapping("/api/session")
    public SesionDto session(Authentication authentication, HttpServletRequest request) {
        final Workspace workspace = workspaceService.getWorkspace(authentication, request);
        final Session session = workspace.getSession();
        return new SesionDto(
                session.getUsername(),
                session.getTenant(),
                session.getTenants() == null ? Set.of() : new TreeSet<>(session.getTenants()),
                session.getLowerDate(),
                session.getUpperDate(),
                session.getInboxes() == null ? Set.of() : new TreeSet<>(session.getInboxes()),
                session.getImportAccounts() == null ? Set.of() : new TreeSet<>(session.getImportAccounts()),
                workspaceService.getMessages(request.getLocale()));
    }

    @Autowired
    ApiSessionController setWorkspaceService(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
        return this;
    }
}

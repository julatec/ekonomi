package name.julatec.ekonomi.api;

import jakarta.servlet.http.HttpServletRequest;
import name.julatec.ekonomi.mcp.AccesoTenant;
import name.julatec.ekonomi.session.Workspace;
import name.julatec.ekonomi.session.WorkspaceService;
import name.julatec.ekonomi.tribunet.storage.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * El catálogo de contrapartes, paginado y filtrable.
 * <p>
 * Antes esta lista viajaba entera dentro de {@code /js/session.js}: se recalculaba en cada
 * login y cada cambio de tenant con una consulta sin {@code Pageable} sobre toda la tabla, y
 * se de-duplicaba en memoria. Acá se pagina y se filtra en la base.
 */
@RestController
@Secured({"ROLE_ADMIN", "ROLE_USER"})
public class ClienteController {

    private static final int TAMANO_POR_DEFECTO = 20;
    private static final int TOPE_TAMANO = 100;

    private FacturaRepository facturas;
    private AccesoTenant accesoTenant;
    private WorkspaceService workspaceService;

    public record ClienteDto(String numero, String nombre, long comprobantes) {
    }

    public record PaginaDto(
            List<ClienteDto> contenido,
            int pagina,
            int tamano,
            long total,
            int totalPaginas) {
    }

    @GetMapping("/api/clients")
    public PaginaDto clientes(
            Authentication authentication,
            HttpServletRequest request,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + TAMANO_POR_DEFECTO) int size) {

        final Workspace workspace = workspaceService.getWorkspace(authentication, request);
        final String tenant = workspace.getSession().getTenant();

        // El patrón se arma acá y no en el SQL para que "sin filtro" sea un `like '%'` y la
        // consulta tenga una sola forma: un `having` condicional obligaría a duplicarla.
        final String patron = nombre == null || nombre.isBlank()
                ? "%"
                : "%" + nombre.trim().toLowerCase() + "%";

        // El orden va en el Pageable, nunca en el SQL: si estuviera en los dos, el que pide
        // la interfaz quedaría de último desempate y no se notaría el efecto.
        final PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, TOPE_TAMANO),
                Sort.by(Sort.Direction.DESC, "comprobantes").and(Sort.by("numero")));

        return accesoTenant.en(tenant, () -> {
            final Page<FacturaRepository.ClienteProyeccion> pagina =
                    facturas.buscarClientes(patron, pageable);
            return new PaginaDto(
                    pagina.getContent().stream()
                            .map(c -> new ClienteDto(c.getNumero(), c.getNombre(),
                                    c.getComprobantes() == null ? 0L : c.getComprobantes()))
                            .toList(),
                    pagina.getNumber(),
                    pagina.getSize(),
                    pagina.getTotalElements(),
                    pagina.getTotalPages());
        });
    }

    @Autowired
    ClienteController setFacturas(FacturaRepository facturas) {
        this.facturas = facturas;
        return this;
    }

    @Autowired
    ClienteController setAccesoTenant(AccesoTenant accesoTenant) {
        this.accesoTenant = accesoTenant;
        return this;
    }

    @Autowired
    ClienteController setWorkspaceService(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
        return this;
    }
}

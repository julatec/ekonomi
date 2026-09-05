package name.julatec.ekonomi.api;

import jakarta.servlet.http.HttpServletRequest;
import name.julatec.ekonomi.mcp.AccesoTenant;
import name.julatec.ekonomi.mcp.BusquedaComprobantes;
import name.julatec.ekonomi.mcp.ComprobanteResumen;
import name.julatec.ekonomi.mcp.FiltroComprobantes;
import name.julatec.ekonomi.mcp.ResumenComprobantes;
import name.julatec.ekonomi.session.Workspace;
import name.julatec.ekonomi.session.WorkspaceService;
import name.julatec.ekonomi.tribunet.Documento;
import name.julatec.ekonomi.tribunet.DocumentoAdapterService;
import name.julatec.ekonomi.tribunet.storage.ElectronicReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Búsqueda de comprobantes para la UI.
 * <p>
 * Devuelve las filas <b>y</b> la banda de totales en una sola respuesta, calculadas con el
 * mismo {@link FiltroComprobantes}: son dos consultas que no se pueden desincronizar. La
 * banda usa {@link ResumenComprobantes} directamente —el servicio honra el filtro completo—
 * y no la herramienta MCP {@code resumen_periodo}, cuya firma solo expone 4 de los 9 filtros.
 * <p>
 * No hay paginación, y es una limitación real del backend: {@code BusquedaComprobantes}
 * consulta con página cero fija, pide el tope a cada una de las cinco tablas y mezcla en
 * memoria. Simular páginas sobre eso sería mentirle a quien lee. En su lugar se informa
 * {@code truncado} y el total, para que la UI ofrezca subir el límite.
 */
@RestController
@Secured({"ROLE_ADMIN", "ROLE_USER"})
public class ComprobanteController {

    /** Igual que la herramienta MCP: por omisión 50, tope 500. */
    private static final int FILAS_POR_DEFECTO = 50;
    private static final int TOPE_FILAS = 500;

    private static final Logger logger = LoggerFactory.getLogger(ComprobanteController.class);

    private BusquedaComprobantes busqueda;
    private ResumenComprobantes resumen;
    private AccesoTenant accesoTenant;
    private WorkspaceService workspaceService;
    private DocumentoAdapterService adapterService;
    private DetalleComprobanteMapper mapper;

    public record BusquedaDto(
            String tenant,
            int total,
            int devueltos,
            boolean truncado,
            Map<String, Long> porTipo,
            List<ComprobanteResumen> comprobantes,
            List<ResumenComprobantes.Linea> lineas) {
    }

    private Set<String> tipos(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        final Set<String> pedidos = new LinkedHashSet<>(
                Arrays.stream(csv.split(",")).map(String::trim).filter(t -> !t.isEmpty()).toList());
        final Set<String> conocidos = busqueda.tiposConocidos();
        for (String tipo : pedidos) {
            if (!conocidos.contains(tipo)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Tipo de comprobante desconocido: " + tipo
                                + ". Válidos: " + String.join(", ", conocidos));
            }
        }
        return pedidos;
    }

    @GetMapping("/api/comprobantes")
    public BusquedaDto buscar(
            Authentication authentication,
            HttpServletRequest request,
            @RequestParam(required = false) String clave,
            @RequestParam(required = false) String consecutivo,
            @RequestParam(required = false) String cedula,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String emisor,
            @RequestParam(required = false) String receptor,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) String montoMinimo,
            @RequestParam(required = false) String montoMaximo,
            @RequestParam(required = false) String moneda,
            @RequestParam(required = false) String tiposDeComprobante,
            @RequestParam(required = false) Integer limite) {

        final FiltroComprobantes filtro;
        try {
            filtro = FiltroComprobantes.de(clave, consecutivo, cedula, nombre,
                    emisor, receptor,
                    desde, hasta, montoMinimo, montoMaximo, moneda);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        if (filtro.vacio()) {
            // Sin ningún criterio esto serían cinco barridos de tabla completos por cada
            // tecla que alguien apriete en la UI.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Hace falta al menos un filtro: fechas, cédula, nombre, clave o consecutivo.");
        }

        final Set<String> tipos = tipos(tiposDeComprobante);
        final int filas = Math.clamp(limite == null ? FILAS_POR_DEFECTO : limite, 1, TOPE_FILAS);

        // El tenant sale de la cookie ya validada contra los datasources del usuario, y se
        // vuelve a validar en AccesoTenant: si no es suyo, falla en vez de caer al primero.
        final Workspace workspace = workspaceService.getWorkspace(authentication, request);
        final String tenant = workspace.getSession().getTenant();

        return accesoTenant.en(tenant, () -> {
            final BusquedaComprobantes.Resultado resultado = busqueda.buscar(filtro, tipos, filas);
            return new BusquedaDto(
                    tenant,
                    resultado.total(),
                    resultado.devueltos(),
                    resultado.truncado(),
                    busqueda.conteosPorTipo(filtro),
                    resultado.comprobantes(),
                    resumen.resumen(filtro, tipos));
        });
    }

    /**
     * El comprobante completo, para el visualizador.
     * <p>
     * Las líneas de detalle y las referencias no están persistidas en forma estructurada: de
     * cada comprobante {@code storage} guarda las partes, el resumen y el XML entero. Así que
     * el detalle sale de re-parsear ese XML con el mismo adaptador que usa la ingesta, que ya
     * resuelve las cuatro generaciones de esquema de Hacienda.
     *
     * @param xml si viene {@code true}, se devuelve además el documento original. Va aparte
     *            porque son decenas de kilobytes que la vista normal no necesita.
     */
    @GetMapping("/api/comprobantes/{clave}")
    public DetalleComprobante detalle(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable String clave,
            @RequestParam(required = false, defaultValue = "false") boolean xml) {

        final Workspace workspace = workspaceService.getWorkspace(authentication, request);
        final String tenant = workspace.getSession().getTenant();

        return accesoTenant.en(tenant, () -> {
            final Map.Entry<String, ElectronicReceipt> encontrado = busqueda.porClave(clave)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No hay ningún comprobante con esa clave en esta contabilidad."));

            final String documentoXml = Optional.ofNullable(encontrado.getValue().getDocumento())
                    .map(d -> d.getDocument())
                    .orElse(null);
            if (documentoXml == null || documentoXml.isBlank()) {
                // La fila existe pero sin el XML no hay líneas que mostrar: es un dato
                // incompleto, no un comprobante inexistente.
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El comprobante está registrado pero no tiene el documento original guardado.");
            }

            final Documento documento = adapterService
                    .adapt(documentoXml, e -> logger.error("No se pudo interpretar el XML de {}", clave, e))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                            "El documento guardado no corresponde a ningún esquema de Hacienda soportado."));

            return mapper.de(tenant, encontrado.getKey(), documento, xml ? documentoXml : null);
        });
    }

    @Autowired
    ComprobanteController setAdapterService(DocumentoAdapterService adapterService) {
        this.adapterService = adapterService;
        return this;
    }

    @Autowired
    ComprobanteController setMapper(DetalleComprobanteMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    @Autowired
    ComprobanteController setBusqueda(BusquedaComprobantes busqueda) {
        this.busqueda = busqueda;
        return this;
    }

    @Autowired
    ComprobanteController setResumen(ResumenComprobantes resumen) {
        this.resumen = resumen;
        return this;
    }

    @Autowired
    ComprobanteController setAccesoTenant(AccesoTenant accesoTenant) {
        this.accesoTenant = accesoTenant;
        return this;
    }

    @Autowired
    ComprobanteController setWorkspaceService(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
        return this;
    }
}

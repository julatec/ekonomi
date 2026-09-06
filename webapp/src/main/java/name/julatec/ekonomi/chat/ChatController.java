package name.julatec.ekonomi.chat;

import jakarta.servlet.http.HttpServletRequest;
import name.julatec.ekonomi.session.Workspace;
import name.julatec.ekonomi.session.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * El asistente: una pregunta en palabras corrientes, y las herramientas del MCP contestándola
 * contra la contabilidad abierta.
 * <p>
 * Corre en el mismo proceso, así que el modelo <b>no</b> recibe credenciales ni llega a la base:
 * pide una herramienta por su nombre, y la herramienta se ejecuta acá con el
 * {@code SecurityContext} de quien preguntó. {@code AccesoTenant} valida el tenant contra
 * {@code user.getDatasources()}, de modo que ni el modelo ni el texto de la pregunta pueden
 * abrir una contabilidad ajena — el peor caso es un error.
 * <p>
 * Sin streaming en esta versión: a ~29 tokens por segundo un intercambio se resuelve en unos
 * segundos y un indicador de espera alcanza. Streaming agregaría un transporte nuevo para
 * ganar percepción, no capacidad.
 */
@RestController
@ConditionalOnProperty(name = "ekonomi.chat.enabled", havingValue = "true", matchIfMissing = true)
public class ChatController {

    private ClienteModelo modelo;
    private WorkspaceService workspaceService;

    @Value("${ekonomi.chat.base-url:http://172.16.13.117:8080}")
    private String baseUrl;

    /** Lo que manda la interfaz. `historial` viaja completo desde el navegador: no hay sesión de chat en el servidor. */
    public record Pregunta(String mensaje, List<Map<String, String>> historial) {
    }

    /**
     * El mensaje de sistema, armado una sola vez y SIEMPRE primero.
     * <p>
     * La plantilla de Ministral rechaza un {@code system} que no esté en la primera posición
     * —devuelve HTTP 500—, así que acá va todo lo que el modelo tiene que saber de entrada: qué
     * contabilidad está abierta, qué día es hoy, y las tres convenciones contables que de otro
     * modo inventaría.
     */
    private String sistema(String tenant) {
        return """
                Sos el asistente de Ekonomi, que guarda comprobantes electrónicos de Costa Rica
                (facturas, facturas de compra, de exportación, y notas de crédito y débito).

                La contabilidad abierta es `%s`. Usá ese tenant en las herramientas salvo que la
                persona pida otro explícitamente. Hoy es %s.

                Reglas que no podés deducir de los datos:
                - Las fechas van AAAA-MM-DD.
                - Los montos están en la moneda de cada documento y NO se consolidan. Si hay
                  varias monedas, reportalas por separado; nunca las sumes entre sí.
                - Las notas de crédito RESTAN de lo facturado, y se reportan aparte a propósito.
                - Una cédula puede ser emisor o receptor. «Vendió» es ser emisor; «compró» es ser
                  receptor.

                Usá las herramientas para responder: no inventes cifras ni las estimes. Si te
                falta un dato para llamar a una herramienta, preguntalo en vez de suponerlo.
                Contestá corto y en español, y decí de dónde salió cada número.
                """.formatted(tenant, LocalDate.now());
    }

    @GetMapping("/api/chat")
    public Map<String, Object> estado() {
        return Map.of(
                "modelo", baseUrl,
                "herramientas", modelo.nombresDeHerramientas());
    }

    @PostMapping("/api/chat")
    public ClienteModelo.Respuesta preguntar(
            Authentication authentication,
            HttpServletRequest request,
            @RequestBody Pregunta pregunta) {

        if (pregunta == null || pregunta.mensaje() == null || pregunta.mensaje().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta la pregunta.");
        }
        final Workspace workspace = workspaceService.getWorkspace(authentication, request);
        final String tenant = workspace.getSession().getTenant();
        return modelo.conversar(
                sistema(tenant),
                pregunta.historial() == null ? List.of() : pregunta.historial(),
                pregunta.mensaje());
    }

    @Autowired
    ChatController setModelo(ClienteModelo modelo) {
        this.modelo = modelo;
        return this;
    }

    @Autowired
    ChatController setWorkspaceService(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
        return this;
    }
}

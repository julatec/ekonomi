package name.julatec.ekonomi.report;

import name.julatec.ekonomi.preferencias.ColumnaSeleccionada;
import name.julatec.ekonomi.preferencias.ColumnasDeReporte;
import name.julatec.ekonomi.preferencias.PreferenciaColumnasService;
import name.julatec.ekonomi.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static name.julatec.ekonomi.preferencias.ColumnasDeReporte.REPORTE_COMPROBANTES;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Qué columnas lleva el auxiliar de compras y ventas, y en qué orden, para quien lo pide.
 * <p>
 * Manda sobre los {@code .xlsx} de {@link ReportController} —{@code /report/sales} y
 * {@code /report/purchases}—, que son los que la interfaz llama «Reportes». <b>No</b> sobre la
 * tabla de comprobantes en pantalla, que arma sus columnas por su cuenta, ni sobre el libro
 * que bajan las herramientas MCP {@code reporte_compras}/{@code reporte_ventas}: ahí el
 * consumidor es un programa y cambiarle el esquema según quién esté conectado sería romperlo a
 * discreción.
 * <p>
 * La preferencia es por persona y no por contabilidad: ver {@code PreferenciaColumnas}.
 */
@RestController
@Secured({"ROLE_ADMIN", "ROLE_USER"})
public class ColumnasReporteController {

    private static final Logger logger = LoggerFactory.getLogger(ColumnasReporteController.class);

    private PreferenciaColumnasService preferencias;

    /** Una columna del reporte: el encabezado tal cual sale en la hoja, y si se escribe. */
    public record ColumnaDto(String columna, boolean visible) {
    }

    /**
     * @param personalizada si hay algo guardado. Con el catálogo entero en {@code columnas}
     *                      —las ocultas también van— no se puede deducir mirando la lista.
     */
    public record ColumnasDto(String reporte, boolean personalizada, List<ColumnaDto> columnas) {
    }

    public record CambioColumnasDto(List<ColumnaDto> columnas) {
    }

    @GetMapping("/api/report/columns")
    public ColumnasDto columnas(Authentication authentication) {
        final String username = username(authentication);
        try {
            return new ColumnasDto(
                    REPORTE_COMPROBANTES,
                    preferencias.tienePreferencia(username, REPORTE_COMPROBANTES),
                    comoDto(preferencias.columnasDe(username, REPORTE_COMPROBANTES)));
        } catch (Exception e) {
            // Acá NO se cae al catálogo por omisión como hace el reporte. Una pantalla que
            // muestra la configuración por omisión cuando en realidad no pudo leer nada deja
            // a alguien acomodando columnas contra una base que no va a poder guardarlas —y
            // si la tabla todavía no existe, el mensaje de la base es lo que lo dice—.
            logger.error("No se pudo leer la preferencia de columnas de {}.", username, e);
            throw new ResponseStatusException(SERVICE_UNAVAILABLE,
                    "No se pudo leer la configuración de columnas: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda el orden completo: las ocultas viajan y se guardan igual, para que apagar una
     * columna y volver a prenderla la devuelva a su lugar y no al final.
     */
    @PutMapping("/api/report/columns")
    public ColumnasDto guardar(Authentication authentication, @RequestBody CambioColumnasDto cambio) {
        final String username = username(authentication);
        final List<ColumnaSeleccionada> columnas = validar(cambio);
        try {
            return new ColumnasDto(
                    REPORTE_COMPROBANTES,
                    true,
                    comoDto(preferencias.guardar(username, REPORTE_COMPROBANTES, columnas)));
        } catch (Exception e) {
            // La base primaria estuvo en `--read-only` hasta donde este código sabe (ver
            // PreferenciaColumnas): un 500 genérico mandaría a buscar el error en el código.
            // El mensaje de la base es lo único que distingue «no se pudo guardar» de «no se
            // puede escribir en esta base», así que viaja.
            logger.error("No se pudo guardar la preferencia de columnas de {}.", username, e);
            throw new ResponseStatusException(SERVICE_UNAVAILABLE,
                    "No se pudo guardar la configuración de columnas: " + e.getMessage(), e);
        }
    }

    /** Vuelve al orden por omisión: borra lo guardado en vez de guardar el catálogo entero. */
    @DeleteMapping("/api/report/columns")
    public ColumnasDto restaurar(Authentication authentication) {
        final String username = username(authentication);
        try {
            preferencias.restaurar(username, REPORTE_COMPROBANTES);
        } catch (Exception e) {
            logger.error("No se pudo restaurar la preferencia de columnas de {}.", username, e);
            throw new ResponseStatusException(SERVICE_UNAVAILABLE,
                    "No se pudo restaurar la configuración de columnas: " + e.getMessage(), e);
        }
        return new ColumnasDto(
                REPORTE_COMPROBANTES, false, comoDto(preferencias.columnasDe(username, REPORTE_COMPROBANTES)));
    }

    /**
     * Rechaza lo que no se puede guardar sin mentirle a alguien: una columna que no existe
     * —se descartaría al leer y la pantalla mostraría otra cosa que lo que se mandó—, una
     * repetida —el orden dejaría de ser uno— y un reporte sin ninguna columna visible, que es
     * un archivo que no se distingue de uno sin datos.
     */
    private static List<ColumnaSeleccionada> validar(CambioColumnasDto cambio) {
        if (cambio == null || cambio.columnas() == null || cambio.columnas().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Hace falta la lista de columnas.");
        }
        final List<ColumnaSeleccionada> columnas = new ArrayList<>(cambio.columnas().size());
        final Set<String> vistas = new HashSet<>();
        for (ColumnaDto columna : cambio.columnas()) {
            if (columna == null || columna.columna() == null || columna.columna().isBlank()) {
                throw new ResponseStatusException(BAD_REQUEST, "Hay una columna sin nombre.");
            }
            if (!ColumnasDeReporte.catalogo().contains(columna.columna())) {
                throw new ResponseStatusException(BAD_REQUEST,
                        "El reporte no tiene ninguna columna «" + columna.columna() + "».");
            }
            if (!vistas.add(columna.columna())) {
                throw new ResponseStatusException(BAD_REQUEST,
                        "La columna «" + columna.columna() + "» viene repetida.");
            }
            columnas.add(new ColumnaSeleccionada(columna.columna(), columna.visible()));
        }
        if (columnas.stream().noneMatch(ColumnaSeleccionada::isVisible)) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "El reporte tiene que llevar al menos una columna.");
        }
        return columnas;
    }

    private static List<ColumnaDto> comoDto(List<ColumnaSeleccionada> columnas) {
        return columnas.stream()
                .map(columna -> new ColumnaDto(columna.getColumna(), columna.isVisible()))
                .toList();
    }

    /**
     * El usuario de la base, no {@code Session.getUsername()} —que devuelve el nombre para
     * mostrar—: lo que se guarda es la llave, y dos personas pueden llamarse igual en pantalla.
     */
    static String username(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "No hay sesión.");
        }
        if (authentication.getPrincipal() instanceof User user) {
            return user.getUsername();
        }
        return authentication.getName();
    }

    /**
     * Devuelve el motivo como JSON, que es lo que la pantalla sabe leer.
     * <p>
     * Sin esto el mensaje no llega: {@code ResponseStatusException} termina en
     * {@code response.sendError(...)}, el contenedor reenvía a {@code /error} y ahí
     * {@code MyErrorController} devuelve la plantilla <b>HTML</b> sin mirar el {@code Accept}.
     * El navegador entonces solo puede decir «la petición falló con 503», y la causa —«la
     * tabla no existe», «la base es de solo lectura»— se queda en el log del servidor. Acá
     * importa al revés: el motivo por el que no se pudo guardar es justamente lo que hay que
     * leer para arreglarlo.
     * <p>
     * Queda acotado a este controlador a propósito: el resto del API tiene el mismo problema y
     * arreglarlo entero es otro cambio.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> comoJson(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of(
                        "status", e.getStatusCode().value(),
                        "message", e.getReason() == null ? "La petición no se pudo completar." : e.getReason()));
    }

    @Autowired
    ColumnasReporteController setPreferencias(PreferenciaColumnasService preferencias) {
        this.preferencias = preferencias;
        return this;
    }
}

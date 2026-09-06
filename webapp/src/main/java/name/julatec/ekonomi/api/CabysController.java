package name.julatec.ekonomi.api;

import name.julatec.ekonomi.cabys.CabysItem;
import name.julatec.ekonomi.cabys.CabysItemRepository;
import name.julatec.ekonomi.cabys.CabysVersion;
import name.julatec.ekonomi.cabys.CabysVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Búsqueda del Catálogo de Bienes y Servicios de Hacienda/BCCR.
 * <p>
 * No pide tenant ni pasa por {@code AccesoTenant}: el catálogo es un estándar público, igual
 * para las contabilidades que abra el certificado. Se autentica igual que el resto de la
 * aplicación —{@code @Secured} exige la sesión, no un tenant— para no dejar un endpoint
 * abierto sin certificado, no porque el contenido dependa de quién pregunta.
 */
@RestController
@Secured({"ROLE_ADMIN", "ROLE_USER"})
public class CabysController {

    private static final int FILAS_POR_DEFECTO = 30;
    private static final int TOPE_FILAS = 200;

    private CabysItemRepository items;
    private CabysVersionRepository versiones;

    public record CabysItemDto(
            String codigo, String descripcion, String categoria1,
            BigDecimal tarifa, boolean exento) {

        static CabysItemDto de(CabysItem item) {
            return new CabysItemDto(
                    item.getCodigo(), item.getDescripcion(), item.getCategoria1(),
                    item.getTarifa(), item.isExento());
        }
    }

    public record BusquedaCabysDto(String version, int devueltos, List<CabysItemDto> items) {
    }

    /**
     * Por código (empieza con lo escrito) o por descripción (la contiene).
     *
     * @param version si se omite, la más reciente cargada — que es lo que casi siempre se
     *                quiere. Sirve para pedir explícitamente una vigencia anterior.
     */
    @GetMapping("/api/cabys")
    public BusquedaCabysDto buscar(
            @RequestParam String q,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) Integer limite) {
        if (q == null || q.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hace falta `q`.");
        }
        final String versionEfectiva = version != null && !version.isBlank()
                ? version
                : versiones.masReciente()
                        .map(CabysVersion::getVersion)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                                "No hay ninguna versión del catálogo CABYS cargada."));
        final int filas = Math.clamp(limite == null ? FILAS_POR_DEFECTO : limite, 1, TOPE_FILAS);
        final List<CabysItemDto> resultado = items
                .buscar(versionEfectiva, q.trim(), PageRequest.of(0, filas, Sort.by("id.codigo")))
                .stream()
                .map(CabysItemDto::de)
                .toList();
        return new BusquedaCabysDto(versionEfectiva, resultado.size(), resultado);
    }

    /** Qué versiones hay cargadas, y cuál está vigente hoy — para mostrarlo en la UI. */
    @GetMapping("/api/cabys/version")
    public List<CabysVersion> versiones() {
        return versiones.findAll(Sort.by(Sort.Direction.DESC, "vigenteDesde"));
    }

    @Autowired
    CabysController setItems(CabysItemRepository items) {
        this.items = items;
        return this;
    }

    @Autowired
    CabysController setVersiones(CabysVersionRepository versiones) {
        this.versiones = versiones;
        return this;
    }
}

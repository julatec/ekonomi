package name.julatec.ekonomi.api;

import name.julatec.ekonomi.actividad.ActividadEconomica;
import name.julatec.ekonomi.actividad.ActividadEconomicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Búsqueda de la correspondencia de actividades económicas ATV (Hacienda) → TRIBU-CR.
 * <p>
 * No pide tenant ni pasa por {@code AccesoTenant}: es un catálogo público de Hacienda, igual
 * para las contabilidades que abra el certificado — el mismo argumento que {@link CabysController}.
 */
@RestController
@Secured({"ROLE_ADMIN", "ROLE_USER"})
public class ActividadController {

    private static final int FILAS_POR_DEFECTO = 30;
    private static final int TOPE_FILAS = 200;

    private ActividadEconomicaRepository actividades;

    public record ActividadEconomicaDto(
            String atv, String atvNombre, String ciiu4, String division,
            String ciiu4Nombre, String especialidad) {

        static ActividadEconomicaDto de(ActividadEconomica actividad) {
            return new ActividadEconomicaDto(
                    actividad.getAtv(), actividad.getAtvNombre(), actividad.getCiiu4(),
                    actividad.getDivision(), actividad.getCiiu4Nombre(), actividad.getEspecialidad());
        }
    }

    public record BusquedaActividadDto(int devueltos, List<ActividadEconomicaDto> actividades) {
    }

    /** Por código ATV (empieza con lo escrito) o por nombre (lo contiene, en cualquiera de los dos). */
    @GetMapping("/api/actividades")
    public BusquedaActividadDto buscar(
            @RequestParam String q,
            @RequestParam(required = false) Integer limite) {
        if (q == null || q.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hace falta `q`.");
        }
        final int filas = Math.clamp(limite == null ? FILAS_POR_DEFECTO : limite, 1, TOPE_FILAS);
        final List<ActividadEconomicaDto> resultado = actividades
                .buscar(q.trim(), PageRequest.of(0, filas, Sort.by("atv", "ciiu4")))
                .stream()
                .map(ActividadEconomicaDto::de)
                .toList();
        return new BusquedaActividadDto(resultado.size(), resultado);
    }

    @Autowired
    ActividadController setActividades(ActividadEconomicaRepository actividades) {
        this.actividades = actividades;
        return this;
    }
}

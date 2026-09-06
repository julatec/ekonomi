package name.julatec.ekonomi.actividad;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActividadEconomicaRepository extends JpaRepository<ActividadEconomica, Long> {

    /**
     * Por código ATV (empieza con lo escrito) o por cualquiera de los dos nombres (lo
     * contiene) — el mismo código de Hacienda puede repetirse en más de una fila si
     * corresponde a varias subclases TRIBU-CR, así que esto no es una llave única.
     */
    @Query("select a from actividadEconomica a where a.atv like concat(:texto, '%') "
            + "or lower(a.atvNombre) like lower(concat('%', :texto, '%')) "
            + "or lower(a.ciiu4Nombre) like lower(concat('%', :texto, '%')) "
            + "order by a.atv, a.ciiu4")
    List<ActividadEconomica> buscar(@Param("texto") String texto, Pageable pageable);
}

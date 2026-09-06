package name.julatec.ekonomi.actividad;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActividadEconomicaRepository extends JpaRepository<ActividadEconomica, Long> {

    /**
     * Por código CIIU4/TRIBU-CR o código ATV (cualquiera de los dos, empieza con lo escrito) o
     * por cualquiera de los dos nombres (lo contiene). Ninguno de los dos códigos es una llave
     * única: el mismo ATV puede repetirse en más de una fila si corresponde a varias subclases
     * CIIU4, y viceversa.
     * <p>
     * CIIU4 va primero en el {@code case} de orden porque es el código que TRIBU-CR —la
     * plataforma vigente de Hacienda— usa hoy; un código que matchea CIIU4 por prefijo es casi
     * siempre la búsqueda exacta que alguien quiso hacer, y debería aparecer antes que una
     * coincidencia de texto en un nombre.
     */
    @Query("select a from actividadEconomica a where a.ciiu4 like concat(:texto, '%') "
            + "or a.atv like concat(:texto, '%') "
            + "or lower(a.atvNombre) like lower(concat('%', :texto, '%')) "
            + "or lower(a.ciiu4Nombre) like lower(concat('%', :texto, '%')) "
            + "order by case "
            + "  when a.ciiu4 like concat(:texto, '%') then 0 "
            + "  when a.atv like concat(:texto, '%') then 1 "
            + "  else 2 end, a.ciiu4, a.atv")
    List<ActividadEconomica> buscar(@Param("texto") String texto, Pageable pageable);
}

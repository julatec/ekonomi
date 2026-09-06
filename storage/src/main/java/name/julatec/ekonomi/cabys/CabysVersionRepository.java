package name.julatec.ekonomi.cabys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface CabysVersionRepository extends JpaRepository<CabysVersion, String> {

    /**
     * La versión vigente en una fecha dada: la de {@link CabysVersion#getVigenteDesde()} más
     * reciente que no sea posterior a esa fecha.
     * <p>
     * Con una sola versión cargada esto siempre devuelve esa, pero la consulta ya está
     * escrita para cuando haya más de una — que es el punto de tener la tabla.
     */
    @Query("select v from cabysVersion v where v.vigenteDesde <= :fecha order by v.vigenteDesde desc")
    java.util.List<CabysVersion> vigentesHasta(@Param("fecha") LocalDate fecha);

    default Optional<CabysVersion> vigenteEn(LocalDate fecha) {
        return vigentesHasta(fecha).stream().findFirst();
    }

    /** La versión más reciente sin importar la fecha: la que usa la búsqueda por omisión. */
    default Optional<CabysVersion> masReciente() {
        return findAll().stream().max(java.util.Comparator.comparing(CabysVersion::getVigenteDesde));
    }
}

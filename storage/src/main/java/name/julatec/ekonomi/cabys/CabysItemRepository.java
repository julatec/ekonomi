package name.julatec.ekonomi.cabys;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CabysItemRepository extends JpaRepository<CabysItem, CabysItemId> {

    Optional<CabysItem> findByIdVersionAndIdCodigo(String version, String codigo);

    /**
     * Por código (empieza con lo escrito) o por descripción (la contiene), dentro de una
     * versión. Un código completo son 13 dígitos; buscar por prefijo deja escribir «0111» y
     * ver toda la rama, que es como se navega la jerarquía en la práctica.
     */
    @Query("select i from cabysItem i where i.id.version = :version "
            + "and (i.id.codigo like concat(:texto, '%') or lower(i.descripcion) like lower(concat('%', :texto, '%'))) "
            + "order by i.id.codigo")
    List<CabysItem> buscar(@Param("version") String version, @Param("texto") String texto, Pageable pageable);
}

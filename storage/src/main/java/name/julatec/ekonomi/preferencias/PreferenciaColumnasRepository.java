package name.julatec.ekonomi.preferencias;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Público —a diferencia de {@code UserRepository}, que es de paquete— porque lo usa
 * {@link PreferenciaColumnasService}, y a través de él los controladores del webapp.
 */
public interface PreferenciaColumnasRepository
        extends JpaRepository<PreferenciaColumnas, PreferenciaColumnasId> {
}

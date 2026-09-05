package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.tribunet.DocumentoAdapterService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * Bootstrap mínimo para las pruebas de {@code storage}: solo necesita descubrir los
 * adaptadores generados de {@code tribunet-adapters} para poder convertir fixtures XML en
 * {@link name.julatec.ekonomi.tribunet.Documento}. {@link Voucher#of} no toca la base de
 * datos, así que se excluye la autoconfiguración JPA/DataSource — no hay motor configurado
 * para las pruebas y no hace falta uno para lo que se está probando aquí.
 */
@SpringBootApplication(
        scanBasePackageClasses = DocumentoAdapterService.class,
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                DataJpaRepositoriesAutoConfiguration.class
        })
public class TestApplication {
}

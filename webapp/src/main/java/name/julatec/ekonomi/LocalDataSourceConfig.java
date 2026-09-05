package name.julatec.ekonomi;

import com.zaxxer.hikari.HikariDataSource;
import name.julatec.ekonomi.storage.MultiRoutingDataSource;
import name.julatec.ekonomi.storage.SecurityConfig;
import name.julatec.ekonomi.storage.StorageConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Datasources del ambiente local, contra el MySQL de {@code docker-compose}.
 * <p>
 * En producción los tres datasources salen de JNDI, que solo existe dentro de un contenedor
 * de servlets con los recursos montados: por eso {@code mvn spring-boot:run} nunca pudo
 * levantar la aplicación. Acá se registran beans con los <b>mismos nombres</b> que
 * {@link StorageConfig#DATASOURCE} y {@link SecurityConfig#DATASOURCE}, aprovechando el
 * {@code spring.main.allow-bean-definition-overriding} que ya estaba puesto.
 * <p>
 * Solo existen bajo el perfil {@code local}. En cualquier otro perfil esta clase no se
 * instancia y los beans JNDI originales siguen siendo los únicos definidos: producción no
 * cambia de comportamiento ni de configuración.
 * <p>
 * Esto <b>no reemplaza</b> al Tomcat de {@code /opt/tomcat}: ahí se prueba el X.509 con la
 * tarjeta contra el esquema real, que es la validación previa al despliegue. Este perfil es
 * para iterar rápido contra datos desechables.
 */
@Configuration
@Profile("local")
public class LocalDataSourceConfig {

    /**
     * 127.0.0.1:<b>3307</b>, no localhost:3306, y el puerto distinto es deliberado.
     * <p>
     * En esta máquina suele haber un túnel SSH {@code -L 3306:julatec.name:3306} apuntando a
     * la base de PRODUCCIÓN, atado a {@code 127.0.0.1:3306}. Con el puerto por omisión de
     * MySQL, {@code localhost} resuelve al túnel antes que al contenedor y este perfil
     * —que corre con {@code hbm2ddl.auto=update}— le haría DDL a la contabilidad real.
     * Pasó: la primera corrida terminó en {@code Access denied for user 'ekonomi'@<IP
     * pública>}, y lo único que la detuvo fue que producción no tiene ese usuario.
     */
    @Value("${name.julatec.ekonomi.local.db.url-prefix:jdbc:mariadb://127.0.0.1:3307/}")
    private String urlPrefix;

    @Value("${name.julatec.ekonomi.local.db.username:ekonomi}")
    private String username;

    @Value("${name.julatec.ekonomi.local.db.password:ekonomi_password}")
    private String password;

    @Value("${name.julatec.ekonomi.local.db.schema-prefix:ekonomi_}")
    private String schemaPrefix;

    @Value("${name.julatec.ekonomi.storage.tenants.default}")
    private String defaultTenant;

    /**
     * Se reusan las claves del mapa de tenants de {@code application.properties} —los valores
     * son nombres JNDI y acá no sirven, pero las claves son los tenants, que es lo que define
     * qué esquemas hay que abrir.
     */
    @Value("#{${name.julatec.ekonomi.storage.tenants}}")
    private Map<String, String> tenants;

    @Value("${name.julatec.ekonomi.local.db.security-schema:primary}")
    private String securitySchema;

    private DataSource datasource(String schema) {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(urlPrefix + schemaPrefix + schema);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setPoolName("local-" + schema);
        dataSource.setMaximumPoolSize(5);
        return dataSource;
    }

    @Bean(SecurityConfig.DATASOURCE)
    public DataSource localSecurityDataSource() {
        return datasource(securitySchema);
    }

    @Bean(StorageConfig.DATASOURCE)
    public DataSource localMultiRoutingDataSource() {
        final Map<Object, Object> targets = new HashMap<>();
        for (String tenant : tenants.keySet()) {
            targets.put(tenant, datasource(tenant));
        }
        final MultiRoutingDataSource multiRoutingDataSource = new MultiRoutingDataSource();
        multiRoutingDataSource.setDefaultTargetDataSource(targets.get(defaultTenant));
        multiRoutingDataSource.setTargetDataSources(targets);
        return multiRoutingDataSource;
    }
}

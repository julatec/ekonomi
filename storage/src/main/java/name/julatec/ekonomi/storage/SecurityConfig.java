package name.julatec.ekonomi.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Properties;


@Configuration(value = SecurityConfig.CONFIGURATION)
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = SecurityConfig.ENTITY_MANAGER_FACTORY,
        transactionManagerRef = SecurityConfig.TRANSACTION_MANAGER,
        basePackageClasses = {
                name.julatec.ekonomi.security.User.class,
                // CABYS y ActividadEconomica comparten esta base y esta unidad de persistencia
                // con la seguridad porque ninguna pertenece a un tenant — ver el javadoc de
                // CabysItem/ActividadEconomica. No son tablas de seguridad; están acá por dónde
                // viven, no por qué son.
                name.julatec.ekonomi.cabys.CabysItem.class,
                name.julatec.ekonomi.actividad.ActividadEconomica.class,
        }
)
public class SecurityConfig {

    public static final String PERSISTENCE_UNIT = "primary";
    public static final String DATASOURCE = PERSISTENCE_UNIT + "DataSource";
    public static final String VENDOR_ADAPTER = PERSISTENCE_UNIT + "JpaVendorAdapter";
    public static final String ENTITY_MANAGER_FACTORY = PERSISTENCE_UNIT + "EntityManagerFactory";
    public static final String TRANSACTION_MANAGER = PERSISTENCE_UNIT + "TransactionManager";
    public static final String CONFIGURATION = PERSISTENCE_UNIT + "Configuration";

    @Value("${name.julatec.ekonomi.storage.security.datasource}")
    private String dataSourceName;

    /**
     * Ver {@link StorageConfig#ddlAuto}: {@code none} contra la base real, {@code update} solo
     * en el ambiente local, que arranca contra un esquema vacío.
     */
    @Value("${name.julatec.ekonomi.storage.ddl-auto:none}")
    private String ddlAuto;

    /**
     * Fuera del perfil {@code local}, donde no hay JNDI: ahí lo reemplaza
     * {@code LocalDataSourceConfig}, que arma el datasource desde propiedades. No se deja al
     * orden de registro de beans que uno gane sobre el otro.
     */
    @Bean(DATASOURCE)
    @Profile("!local")
    public DataSource dataSource() throws NamingException {
        return (DataSource) new JndiTemplate().lookup(dataSourceName);
    }

    @Bean(ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier(DATASOURCE) DataSource dataSource,
            @Qualifier(VENDOR_ADAPTER) JpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setPersistenceUnitName(PERSISTENCE_UNIT);
        lef.setPackagesToScan(Arrays.stream(this.getClass().getAnnotationsByType(EnableJpaRepositories.class))
                .map(EnableJpaRepositories::basePackageClasses)
                .flatMap(Arrays::stream)
                .map(Class::getPackageName)
                .toArray(String[]::new));
        lef.setDataSource(dataSource);
        lef.setJpaVendorAdapter(jpaVendorAdapter);
        lef.setJpaProperties(getJpaProperties());
        lef.setPackagesToScan(
                "name.julatec.ekonomi.security", "name.julatec.ekonomi.cabys", "name.julatec.ekonomi.actividad");
        return lef;
    }

    @Bean(VENDOR_ADAPTER)
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.MYSQL);
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(false);
        jpaVendorAdapter.setDatabasePlatform(StorageConfig.DATABASE_PLATFORM);
        return jpaVendorAdapter;
    }

    private Properties getJpaProperties() {
        final Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
        properties.setProperty("hibernate.dialect", StorageConfig.DATABASE_PLATFORM);
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "true");
        return properties;
    }

    @Primary
    @Bean(TRANSACTION_MANAGER)
    public PlatformTransactionManager dbTransactionManager(
            @Qualifier(ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}

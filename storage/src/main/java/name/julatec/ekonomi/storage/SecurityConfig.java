package name.julatec.ekonomi.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
                name.julatec.ekonomi.security.User.class
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

    @Bean(DATASOURCE)
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
        return lef;
    }

    @Bean(VENDOR_ADAPTER)
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.MYSQL);
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(false);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        return jpaVendorAdapter;
    }

    private Properties getJpaProperties() {
        return new Properties() {
            {
                setProperty("hibernate.hbm2ddl.auto", "update");
                setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
                setProperty("hibernate.show_sql", "false");
                setProperty("hibernate.format_sql", "true");
            }
        };
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

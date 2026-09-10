package name.julatec.ekonomi.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Carga el {@link Issuer} y el {@link User} de Killa -- el certificado de
 * servicio con el que Django se autentica ante Ekonomi -- una sola vez, a
 * mano, nunca en cada arranque.
 * <p>
 * Mismo motivo que {@code SembradorComprobantes} (paquete
 * {@code name.julatec.ekonomi.local}): usa {@link UserRepository} e
 * {@link IssuerRepository} en vez de escribir un INSERT a mano. Los nombres
 * de columna de las {@code @ElementCollection} ({@code ids}, {@code roles})
 * los decide Hibernate; adivinarlos es el atajo que después miente.
 * <p>
 * Perfil propio, {@code seed-killa}, no {@code local}: {@code local} usa
 * datasources de Docker Compose (ver {@code LocalDataSourceConfig}), que
 * no existen en producción -- {@code mvn spring-boot:run} nunca llega a la
 * base real. Esto tiene que correr <b>dentro del WAR real, en el Tomcat
 * real de {@code /opt/tomcat}</b>, con las mismas datasources JNDI de
 * siempre, activando sólo el perfil {@code seed-killa} por encima del que
 * ya esté activo -- una vez, y no se vuelve a usar.
 * <p>
 * Idempotente por construcción: {@code save()} sobre una entidad con la
 * misma {@code @Id} actualiza en vez de duplicar, así que correrlo dos
 * veces no rompe nada -- mismo criterio que {@code SembradorComprobantes}.
 * <p>
 * {@link AuthenticationService} arma su mapa de issuers/usuarios una sola
 * vez, en el constructor (ver su propio Javadoc) -- estas filas no las ve
 * la aplicación que ya está corriendo cuando esto se ejecuta. Hace falta
 * un reinicio aparte, sin el perfil {@code seed-killa}, para que las tome.
 */
@Component
@Profile("seed-killa")
public class SembradorKilla implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SembradorKilla.class);

    static final String ISSUER_NAME = "Bioinfcr CA Servicios";
    static final String ISSUER_FIELD = "CN";
    static final String USERNAME = "killa-django";
    static final String USER_ID_VALUE = "inti.bioinfcr.org";
    static final String ROLE = "ROLE_KILLA";

    private final UserRepository userRepository;
    private final IssuerRepository issuerRepository;

    public SembradorKilla(UserRepository userRepository, IssuerRepository issuerRepository) {
        this.userRepository = userRepository;
        this.issuerRepository = issuerRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Nunca revienta el arranque -- mismo criterio que core.telegram en
        // Inti: un ApplicationRunner que lanza tumba TODO el contexto de
        // Spring, no sólo la siembra. Pasó de verdad la primera vez que
        // corrió esto: la base estaba en --read-only (ya conocido, aparte de
        // esta tarea) y la excepción bajó a killa entero.
        try {
            issuerRepository.save(new Issuer().setName(ISSUER_NAME).setField(ISSUER_FIELD));

            final User usuario = new User()
                    .setUsername(USERNAME)
                    .setDisplayName("Killa (Django, servicio)")
                    .setIds(Set.of(new UserId().setIssuer(ISSUER_NAME).setValue(USER_ID_VALUE)))
                    .setRoles(Set.of(ROLE));
            // setDatasources() no es fluido como el resto de los setters de
            // User (devuelve void, no this) -- no se puede encadenar arriba.
            usuario.setDatasources(Set.of());
            userRepository.save(usuario);

            logger.info(
                    "Killa sembrado: Issuer[{}] + User[{}], ids={}, roles={}. Hace falta "
                            + "reiniciar SIN el perfil seed-killa para que AuthenticationService "
                            + "vea las filas nuevas -- las carga una sola vez, en el constructor.",
                    ISSUER_NAME, USERNAME, usuario.getIds(), usuario.getRoles());
        } catch (Exception error) {
            logger.error(
                    "No se pudo sembrar Killa (Issuer/User) -- Killa sigue devolviendo 401 "
                            + "hasta que esto se resuelva y se reintente. killa arranca igual.",
                    error);
        }
    }
}

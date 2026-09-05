package name.julatec.ekonomi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * El agendamiento queda fuera del perfil {@code local} a propósito.
 * <p>
 * {@code ExtractService} lleva {@code @Scheduled(fixedDelay = 6 h)} y se dispara también al
 * arrancar: levantar la aplicación en la Mac ponía a procesar los cuatro buzones reales en
 * paralelo con la instancia del Pi. Iterando sobre la UI eso pasa muchas veces por hora, y
 * los mappers persisten sin loguear, así que un duplicado no se vería.
 * <p>
 * El perfil no alcanzaba. Queda un caso que el {@code @Profile} no cubre y que es justo el
 * más peligroso: desplegar el WAR en un Tomcat apuntado a la contabilidad REAL —para mirar
 * datos de verdad en la interfaz— corre sin el perfil {@code local}, así que la ingesta se
 * prendía sola. Con la propiedad se apaga sin inventar un perfil nuevo ni tocar los
 * datasources:
 * <pre>
 *   -Dname.julatec.ekonomi.scheduling.enabled=false
 * </pre>
 * Por omisión sigue encendida: en el Pi, que es quien debe ingerir, no hay que poner nada.
 */
@Configuration
@Profile("!local")
@ConditionalOnProperty(
        name = "name.julatec.ekonomi.scheduling.enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableScheduling
public class SchedulingConfig {
}

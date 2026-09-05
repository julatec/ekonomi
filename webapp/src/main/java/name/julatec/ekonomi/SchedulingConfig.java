package name.julatec.ekonomi;

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
 */
@Configuration
@Profile("!local")
@EnableScheduling
public class SchedulingConfig {
}

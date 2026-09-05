package name.julatec.ekonomi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * La seguridad por método viene APAGADA, y no es un olvido.
 * <p>
 * En producción la tabla {@code user_roles} tiene <b>cero filas</b> — verificado contra la base
 * el 1 sep 2026 y anotado en {@code SEGURIDAD-URGENTE-2026-08-28.md}, donde la decisión quedó
 * escrita como «no se activa … la anotación se queda comentada a propósito». Todas las demás
 * tablas de usuario están pobladas, así que no es un error de lectura: nadie tiene rol.
 * <p>
 * Con {@code @EnableMethodSecurity} encendido, los {@code @Secured({"ROLE_ADMIN","ROLE_USER"})}
 * de {@code IndexController} y de los controladores del API dejan fuera a <b>los dos usuarios
 * de producción, incluido el dueño</b>. Se comprobó en vivo el 5 sep 2026 contra la base real:
 * <pre>
 *   /report/sales   (solo authenticated)   200   ← el certificado autentica bien
 *   /api/session    (@Secured)             403
 *   /               (@Secured)             403
 * </pre>
 * <p>
 * <b>Requisito para prenderla:</b> poblar {@code user_roles} decidiendo quién es ADMIN y quién
 * USER. Es una tarea de datos, no de código. Hecho eso:
 * <pre>
 *   -Dname.julatec.ekonomi.method-security.enabled=true
 * </pre>
 * y reiniciar — {@code AuthenticationService} arma su mapa de usuarios una sola vez, en el
 * constructor, así que insertar filas con la aplicación arriba no cambia nada.
 * <p>
 * Queda como propiedad y no como anotación comentada para que prenderla no exija recompilar,
 * y para que el motivo viaje con el código en vez de vivir solo en un documento.
 */
@Configuration
@ConditionalOnProperty(
        name = "name.julatec.ekonomi.method-security.enabled",
        havingValue = "true")
@EnableMethodSecurity(securedEnabled = true)
public class MethodSecurityConfig {
}

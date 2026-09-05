package name.julatec.ekonomi.session;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sirve la interfaz.
 * <p>
 * Ya no renderiza una plantilla: reenvía al {@code index.html} que construye Vite. La
 * plantilla anterior no tenía una sola directiva de Thymeleaf —era HTML estático pasando por
 * el motor sin aprovecharlo—, así que reenviar al archivo construido evita copiarlo a
 * {@code templates/} en cada build y evita tener que reescribirle las rutas de los assets.
 * <p>
 * El nombre de usuario tampoco se inyecta ya en el modelo: la interfaz lo pide por
 * {@code GET /api/session}, junto con el resto de los datos de arranque.
 * <p>
 * Las rutas internas ({@code /comprobantes/…}, {@code /clientes}) las resuelve el enrutador
 * del navegador, pero si alguien recarga o pega un enlace la petición llega primero acá: por
 * eso todas reenvían al mismo documento.
 */
@Controller
@Secured({"ROLE_ADMIN", "ROLE_USER"})
public class IndexController {

    private static final String APLICACION = "forward:/dist/index.html";

    @GetMapping({"/", "/index.html", "/comprobantes", "/comprobantes/**", "/clientes"})
    public String home() {
        return APLICACION;
    }
}

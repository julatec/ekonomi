package name.julatec.ekonomi.session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Descarta el tenant activo al terminar cada petición.
 * <p>
 * {@link MultiTenantRepository} guarda el tenant en un {@code ThreadLocal} y
 * Tomcat reutiliza los hilos entre peticiones. Sin esta limpieza, un hilo que
 * atendió al tenant A conservaba ese valor, y la siguiente petición que llegara
 * a ese mismo hilo antes de fijar el suyo leía la base equivocada.
 * <p>
 * Se registra como el filtro más externo para que su {@code finally} envuelva
 * toda la petición, incluidos los filtros de Spring Security.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            MultiTenantRepository.clear();
        }
    }
}

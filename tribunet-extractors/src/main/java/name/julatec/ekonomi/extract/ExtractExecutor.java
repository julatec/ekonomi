package name.julatec.ekonomi.extract;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Pool de hilos propio del contexto de la aplicación para el trabajo de extracción.
 *
 * <p>Existe por una razón concreta. Un {@code parallel stream} corre en
 * {@link java.util.concurrent.ForkJoinPool#commonPool()}, cuyos hilos son globales de la JVM y
 * <b>no pertenecen al ciclo de vida del webapp</b>. Al hacer undeploy/redeploy en Tomcat esos
 * hilos siguen vivos, intentan cargar clases de un {@code WebappClassLoader} ya cerrado, fallan
 * con {@code IllegalStateException: Illegal access: this web application instance has been
 * stopped already}, registran la excepción y reintentan en bucle. En el Raspberry Pi de
 * producción eso dejó un {@code catalina.out} de 110 GB.
 *
 * <p>Este pool, en cambio, nace y muere con el contexto de Spring: {@link #shutdown()} corre en
 * {@code @PreDestroy}, o sea antes de que Tomcat cierre el classloader del webapp.
 *
 * <p>Al cerrar se abandona el trabajo en vuelo a propósito: el lote lo relanza el
 * {@code @Scheduled} de {@link ExtractService} dentro de 6 h, así que no vale la pena demorar
 * un redespliegue por él.
 *
 * @see <a href="https://github.com/julatec/ekonomi">diagnóstico: config/mtls-firma-digital.md §5.2</a>
 */
@Component
public class ExtractExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ExtractExecutor.class);

    /** Prefijo del nombre de los hilos. Sale en los stack traces; que se lea de dónde viene. */
    static final String THREAD_PREFIX = "ekonomi-extract-";

    /** Margen para que las tareas en vuelo terminen solas antes de interrumpirlas. */
    private static final long GRACE_SECONDS = 5;

    /** Margen para que mueran ya interrumpidas. Pasado esto solo queda dejar constancia. */
    private static final long FORCE_SECONDS = 10;

    private final ThreadPoolExecutor executor;

    /**
     * Volátil: lo escribe el hilo que cierra el contexto y lo leen los hilos del pool y el que
     * encola. Sirve para que nadie siga encolando ni registrando durante el cierre.
     */
    private volatile boolean shuttingDown = false;

    public ExtractExecutor(@Value("${ekonomi.extract.threads:0}") int threads) {
        final int size = threads > 0
                ? threads
                : Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors()));
        this.executor = new ThreadPoolExecutor(
                size, size,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory());
        logger.info("Pool de extracción propio del webapp iniciado con {} hilos '{}*'", size, THREAD_PREFIX);
    }

    private static ThreadFactory threadFactory() {
        // El classloader del webapp, capturado ahora: es el que debe ver el hilo, no el que
        // resulte estar activo cuando el pool cree el hilo por demanda.
        final ClassLoader webappClassLoader = ExtractExecutor.class.getClassLoader();
        final AtomicInteger counter = new AtomicInteger();
        return runnable -> {
            final Thread thread = new Thread(runnable, THREAD_PREFIX + counter.incrementAndGet());
            thread.setContextClassLoader(webappClassLoader);
            // Daemon como red de seguridad: si algún día se escapa uno, no traba el cierre de la JVM.
            thread.setDaemon(true);
            return thread;
        };
    }

    /** {@code true} cuando el contexto se está cerrando; las tareas largas deberían rendirse. */
    public boolean isShuttingDown() {
        return shuttingDown || Thread.currentThread().isInterrupted();
    }

    /**
     * Aplica {@code action} a cada elemento en el pool y <b>no regresa hasta que todas las
     * tareas terminaron</b>. Reemplaza a {@code stream.parallel().forEach(action)}.
     *
     * <p>Esperar no es opcional: quien llama suele cerrar el recurso del que salen los elementos
     * (p. ej. {@code StoreCommand} cierra el {@code Store} apenas regresa el folder).
     */
    public <T> void forEach(Stream<T> items, Consumer<? super T> action) {
        final List<Future<?>> pending = new ArrayList<>();
        boolean rejected = false;
        try {
            final Iterator<T> iterator = items.iterator();
            while (iterator.hasNext()) {
                final T item = iterator.next();
                if (isShuttingDown()) {
                    break;
                }
                try {
                    pending.add(executor.submit(() -> action.accept(item)));
                } catch (RejectedExecutionException e) {
                    // El pool cerró mientras encolábamos. Una línea y afuera: insistir acá es
                    // justamente lo que produjo los 110 GB de log.
                    rejected = true;
                    break;
                }
            }
        } finally {
            if (rejected) {
                logger.warn("Pool de extracción cerrado durante el encolado; se descarta el resto del lote.");
            }
            awaitAll(pending);
        }
    }

    /** Espera cada tarea, registrando fallos individuales sin abortar el resto del lote. */
    private void awaitAll(List<Future<?>> pending) {
        for (int i = 0; i < pending.size(); i++) {
            try {
                pending.get(i).get();
            } catch (ExecutionException e) {
                logger.error("Tarea de extracción terminada con error", e.getCause());
            } catch (CancellationException e) {
                // Cancelada por el cierre; ya se registró en shutdown().
            } catch (InterruptedException e) {
                // Nos están cerrando: cancelar lo que queda, restaurar la bandera y salir.
                for (int j = i; j < pending.size(); j++) {
                    pending.get(j).cancel(true);
                }
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Cierra el pool antes de que Tomcat cierre el classloader.
     *
     * <p>Spring administra este bean, así que {@code @PreDestroy} basta y no hace falta un
     * {@code ServletContextListener} aparte: {@code SpringBootServletInitializer} registra el
     * cierre del contexto contra el del {@code ServletContext}.
     */
    @PreDestroy
    public void shutdown() {
        if (shuttingDown) {
            return;
        }
        shuttingDown = true;
        executor.shutdown();
        try {
            if (executor.awaitTermination(GRACE_SECONDS, TimeUnit.SECONDS)) {
                logger.info("Pool de extracción cerrado limpiamente.");
                return;
            }
            final List<Runnable> descartadas = executor.shutdownNow();
            logger.warn("El pool de extracción no terminó en {} s: interrumpiendo ({} tareas sin empezar).",
                    GRACE_SECONDS, descartadas.size());
            if (!executor.awaitTermination(FORCE_SECONDS, TimeUnit.SECONDS)) {
                logger.error("Quedan hilos '{}*' vivos tras {} s de interrupción. "
                                + "Si el log vuelve a crecer sin control, buscar acá.",
                        THREAD_PREFIX, FORCE_SECONDS);
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

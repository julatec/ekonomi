package name.julatec.ekonomi.extract;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de regresión del defecto que llenó {@code catalina.out} con 110 GB en producción:
 * el trabajo de extracción corría en {@code ForkJoinPool.commonPool()}, cuyos hilos sobreviven
 * al undeploy del webapp.
 */
class ExtractExecutorTest {

    private ExtractExecutor executor;

    @AfterEach
    void cerrar() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    /** Devuelve los hilos vivos creados por el pool, por nombre. */
    private static List<String> hilosDelPoolVivos() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive)
                .map(Thread::getName)
                .filter(name -> name.startsWith(ExtractExecutor.THREAD_PREFIX))
                .toList();
    }

    @Test
    @DisplayName("forEach ejecuta todas las tareas y no regresa hasta que terminaron")
    void esperaATodoElLote() {
        executor = new ExtractExecutor(4);
        final AtomicInteger hechas = new AtomicInteger();

        executor.forEach(IntStream.range(0, 500).boxed(), item -> hechas.incrementAndGet());

        // Sin espera esto sería flaky; el punto de la prueba es que forEach ya esperó.
        assertEquals(500, hechas.get(),
                "forEach debe bloquear hasta que todo el lote terminó: StoreCommand cierra el Store apenas regresa");
    }

    @Test
    @DisplayName("el trabajo NO corre en ForkJoinPool.commonPool()")
    void noUsaElCommonPool() {
        executor = new ExtractExecutor(4);
        final Set<String> hilos = ConcurrentHashMap.newKeySet();

        executor.forEach(IntStream.range(0, 200).boxed(),
                item -> hilos.add(Thread.currentThread().getName()));

        assertFalse(hilos.isEmpty(), "alguna tarea debió correr");
        assertTrue(hilos.stream().noneMatch(name -> name.contains("commonPool")),
                "ninguna tarea puede correr en el common pool; corrieron en: " + hilos);
        assertTrue(hilos.stream().allMatch(name -> name.startsWith(ExtractExecutor.THREAD_PREFIX)),
                "todas deben correr en el pool propio del webapp; corrieron en: " + hilos);
    }

    @Test
    @DisplayName("shutdown() no deja hilos huérfanos vivos tras el undeploy")
    void shutdownNoDejaHilosHuerfanos() throws InterruptedException {
        executor = new ExtractExecutor(4);
        executor.forEach(IntStream.range(0, 50).boxed(), item -> { });
        assertFalse(hilosDelPoolVivos().isEmpty(), "el pool debió crear hilos para poder probarlos");

        executor.shutdown();

        // Los hilos mueren de forma asíncrona; se le da un margen razonable.
        final long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (!hilosDelPoolVivos().isEmpty() && System.nanoTime() < limite) {
            Thread.sleep(50);
        }
        assertTrue(hilosDelPoolVivos().isEmpty(),
                "tras shutdown() no puede quedar ningún hilo del pool vivo; quedaron: " + hilosDelPoolVivos());
        assertTrue(executor.isShuttingDown(), "isShuttingDown() debe quedar en true tras cerrar");
    }

    @Test
    @DisplayName("shutdown() interrumpe una tarea colgada en vez de esperarla para siempre")
    void shutdownInterrumpeTareasColgadas() throws InterruptedException {
        executor = new ExtractExecutor(2);
        final CountDownLatch arrancó = new CountDownLatch(1);
        final CountDownLatch interrumpida = new CountDownLatch(1);

        final Thread lote = new Thread(() -> executor.forEach(Stream.of(1), item -> {
            arrancó.countDown();
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(10));
            } catch (InterruptedException e) {
                interrumpida.countDown();
                Thread.currentThread().interrupt();
            }
        }));
        lote.setDaemon(true);
        lote.start();
        assertTrue(arrancó.await(10, TimeUnit.SECONDS), "la tarea debió arrancar");

        executor.shutdown();

        assertTrue(interrumpida.await(20, TimeUnit.SECONDS),
                "shutdownNow() debe interrumpir la tarea en vuelo, no esperarla");
        lote.join(TimeUnit.SECONDS.toMillis(20));
        assertFalse(lote.isAlive(), "forEach debe soltar al hilo que esperaba el lote");
    }

    @Test
    @DisplayName("tras shutdown(), forEach no encola nada y regresa de una")
    void trasShutdownNoEncolaMas() {
        executor = new ExtractExecutor(2);
        executor.shutdown();
        final AtomicInteger hechas = new AtomicInteger();

        executor.forEach(IntStream.range(0, 100).boxed(), item -> hechas.incrementAndGet());

        assertEquals(0, hechas.get(), "con el contexto cerrado no se debe ejecutar ni encolar trabajo nuevo");
    }

    @Test
    @DisplayName("una tarea que falla no aborta el resto del lote")
    void unFalloNoTumbaElLote() {
        executor = new ExtractExecutor(4);
        final AtomicInteger hechas = new AtomicInteger();

        executor.forEach(IntStream.range(0, 100).boxed(), item -> {
            if (item % 10 == 0) {
                throw new IllegalStateException("falla simulada en " + item);
            }
            hechas.incrementAndGet();
        });

        assertEquals(90, hechas.get(), "las tareas sanas deben completarse aunque otras revienten");
    }
}

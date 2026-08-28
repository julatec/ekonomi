package name.julatec.ekonomi.extract;

import jakarta.mail.Folder;
import name.julatec.ekonomi.extract.command.CommandFactory;
import name.julatec.ekonomi.extract.command.Context;
import name.julatec.ekonomi.extract.command.FolderCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Verifica el cableado y el ciclo de vida, que es donde este arreglo se puede caer en silencio:
 * si {@link ExtractExecutor} no fuera inyectable, {@code FolderCommand} reventaría al crearse
 * dentro del lote — y {@code InboxCommand} se traga la excepción, así que la extracción quedaría
 * muerta sin que se note.
 */
class ExtractWiringTest {

    @Configuration
    static class Config {

        @Bean
        static PropertySourcesPlaceholderConfigurer placeholders() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        CommandFactory commandFactory() {
            return mock(CommandFactory.class);
        }

        @Bean
        ExtractExecutor extractExecutor() {
            return new ExtractExecutor(2);
        }
    }

    private static List<String> hilosDelPoolVivos() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive)
                .map(Thread::getName)
                .filter(name -> name.startsWith(ExtractExecutor.THREAD_PREFIX))
                .toList();
    }

    @Test
    @DisplayName("Spring inyecta el ExtractExecutor en el prototype FolderCommand")
    void folderCommandRecibeElExecutor() throws Exception {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(Config.class, FolderCommand.class);
            ctx.refresh();

            // Si el executor no fuera resoluble, esto tiraría UnsatisfiedDependencyException.
            final FolderCommand command = ctx.getBean(
                    FolderCommand.class,
                    new Context<>(null, LoggerFactory.getLogger(ExtractWiringTest.class)),
                    mock(Folder.class));

            final Field field = FolderCommand.class.getDeclaredField("extractExecutor");
            field.setAccessible(true);
            assertNotNull(field.get(command), "el setter @Autowired debe correr sobre el prototype");
            assertSame(ctx.getBean(ExtractExecutor.class), field.get(command),
                    "debe compartir el pool del contexto, no crear uno por comando");
        }
    }

    @Test
    @DisplayName("cerrar el contexto dispara @PreDestroy y apaga el pool")
    void cerrarElContextoApagaElPool() throws InterruptedException {
        final ExtractExecutor executor;
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(Config.class);
            ctx.refresh();
            executor = ctx.getBean(ExtractExecutor.class);
            executor.forEach(List.of(1, 2, 3).stream(), item -> { });
            assertFalse(executor.isShuttingDown(), "con el contexto arriba el pool debe estar operativo");
            assertFalse(hilosDelPoolVivos().isEmpty(), "el pool debió crear hilos para poder probarlos");
        }

        // Salir del try cierra el contexto: es el equivalente al undeploy de Tomcat.
        assertTrue(executor.isShuttingDown(), "@PreDestroy debe haber corrido al cerrar el contexto");

        final long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (!hilosDelPoolVivos().isEmpty() && System.nanoTime() < limite) {
            Thread.sleep(50);
        }
        assertTrue(hilosDelPoolVivos().isEmpty(),
                "el undeploy no puede dejar hilos vivos; quedaron: " + hilosDelPoolVivos());
    }
}

package name.julatec.ekonomi.extract;

import name.julatec.ekonomi.extract.command.BaseCommand;
import name.julatec.ekonomi.extract.command.CommandFactory;
import name.julatec.ekonomi.extract.command.Context;
import name.julatec.ekonomi.extract.command.InboxCommand;
import name.julatec.ekonomi.security.Inbox;
import name.julatec.ekonomi.security.InboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ExtractService extends BaseCommand<ExtractService> {

    private static final Logger logger = LoggerFactory.getLogger(ExtractService.class);

    final CommandFactory commandFactory;

    final InboxRepository inboxRepository;

    final ExtractExecutor extractExecutor;

    public ExtractService(
            CommandFactory commandFactory,
            InboxRepository inboxRepository,
            ExtractExecutor extractExecutor) {
        super(new Context<ExtractService>(null, logger));
        this.commandFactory = commandFactory;
        this.inboxRepository = inboxRepository;
        this.extractExecutor = extractExecutor;
    }

    private void startListen(Inbox inbox) {
        try {
            final InboxCommand command = commandFactory.getCommand(this, inbox);
            command.run();
        } catch (Exception e) {
            logger.error("Unable to process {}", inbox.getEmail(), e);
        }

    }

    /**
     * Recorre los buzones <b>de uno en uno</b>, a propósito.
     *
     * <p>Antes esto era un {@code parallel stream}, con los dos defectos que trae: corría en
     * {@link java.util.concurrent.ForkJoinPool#commonPool()}, cuyos hilos sobreviven al undeploy
     * del webapp, y anidaba paralelismo con el de {@code FolderCommand}.
     *
     * <p>El paralelismo ahora vive un nivel más adentro: {@code FolderCommand} reparte los
     * mensajes de cada buzón en {@link ExtractExecutor}. Anidar ahí también sería un abrazo
     * mortal — las tareas de buzón ocuparían todos los hilos del pool esperando tareas de
     * mensaje que ya no tendrían dónde correr. Y como es un lote cada 6 h, serializar los
     * buzones no cuesta nada.
     */
    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000)
    @Override
    public void run() {
        if (extractExecutor.isShuttingDown()) {
            logger.info("Contexto en cierre: se omite esta corrida de extracción.");
            return;
        }
        for (Inbox inbox : inboxRepository.findAll()) {
            if (extractExecutor.isShuttingDown()) {
                logger.info("Contexto en cierre: se corta la corrida de extracción.");
                return;
            }
            if (inbox.isActive()) {
                startListen(inbox);
            }
        }
    }
}

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

    public ExtractService(CommandFactory commandFactory, InboxRepository inboxRepository) {
        super(new Context<ExtractService>(null, logger));
        this.commandFactory = commandFactory;
        this.inboxRepository = inboxRepository;
    }

    private void startListen(Inbox inbox) {
        try {
            final InboxCommand command = commandFactory.getCommand(this, inbox);
            command.run();
        } catch (Exception e) {
            logger.error("Unable to process {}", inbox.getEmail(), e);
        }

    }

    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000)
    @Override
    public void run() {
        inboxRepository
                .findAll()
                .stream()
                .parallel()
                .filter(Inbox::isActive)
                .forEach(this::startListen);
    }
}

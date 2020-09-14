package name.julatec.ekonomi.extract.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.ReceivedDateTerm;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static javax.mail.search.ComparisonTerm.GE;
import static name.julatec.ekonomi.extract.command.InboxCommand.EMAIL_ATTRIBUTE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class FolderCommand extends BaseCommand<FolderCommand> {

    protected final Folder folder;

    private Environment environment;

    public FolderCommand(Context<?> context, Folder folder) {
        super(context);
        this.folder = folder;
    }

    private Stream<Message> getAllMessages() throws MessagingException {
        return Arrays.stream(folder.getMessages());
    }

    private Stream<Message> getMessagesSince(long units, TimeUnit timeUnit) throws MessagingException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, (int) timeUnit.toDays(units));
        final Date dateLimit = calendar.getTime();
        return Arrays.stream(folder.search(new ReceivedDateTerm(GE, dateLimit)))
                .filter(message -> {
                    final Date receivedDate;
                    try {
                        receivedDate = message.getReceivedDate();
                    } catch (MessagingException e) {
                        getLogger().error("[{}] Error reading messages", context.getAttribute(EMAIL_ATTRIBUTE), e);
                        return false;
                    }
                    if (receivedDate.before(dateLimit)) {
                        getLogger().info("[{}] Ignoring message from {} < {}",
                                context.getAttribute(EMAIL_ATTRIBUTE),
                                receivedDate,
                                dateLimit);
                        return false;
                    }
                    return true;
                });
    }

    private void process(Message message) {
        try {
            if (!folder.isOpen()) {
                folder.open(Folder.READ_ONLY);
            }
            final Date receivedDate = message.getReceivedDate();
            if (message.getContentType().contains("multipart")) {
                getLogger().info("[{}][{}/{}]: {}",
                        context.getAttribute(EMAIL_ATTRIBUTE),
                        receivedDate,
                        message.getMessageNumber(), message.getSubject());
                if (message.getSubject().contains("00200001010000015211")) {
                    getLogger().info("[{}] stop", context.getAttribute(EMAIL_ATTRIBUTE));
                }
                final MessageCommand command = commandFactory.getCommand(this, message);
                command.run();
            } else {
                getLogger().info("[{}] Ignoring message from {}", receivedDate, context.getAttribute(EMAIL_ATTRIBUTE));
            }
        } catch (Exception e) {
            getLogger().error("[{}] Error reading messages", context.getAttribute(EMAIL_ATTRIBUTE), e);
        }
    }

    protected Stream<Message> getMessages() throws MessagingException {
        final Set<String> profiles = Set.of(environment.getActiveProfiles());
        return profiles.contains("production") ? getMessagesSince(-1, TimeUnit.DAYS) :
                profiles.contains("month-import") ? getMessagesSince(-45, TimeUnit.DAYS) :
                        profiles.contains("full-import") ? getAllMessages() :
                                Stream.empty();
    }


    @Override
    public void run() {
        try {
            folder.open(Folder.READ_ONLY);
            getMessages()
                    .parallel()
                    .forEach(this::process);
        } catch (MessagingException e) {
            getLogger().error("[{}] Error reading messages", context.getAttribute(EMAIL_ATTRIBUTE), e);
        } catch (Exception e) {
            getLogger().error("[{}] Error reading messages", context.getAttribute(EMAIL_ATTRIBUTE), e);
        }

    }


    @Autowired
    FolderCommand setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }
}

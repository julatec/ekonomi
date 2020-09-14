package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.security.Inbox;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.mail.Session;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class InboxCommand extends BaseCommand<InboxCommand> {

    public static final String EMAIL_ATTRIBUTE = "email";
    public static final String TENANTS_ATTRIBUTE = "tenants";

    protected final Inbox inbox;

    public InboxCommand(Context<InboxCommand> context, Inbox inbox) {
        super(context);
        this.inbox = inbox;
        context.setAttribute(EMAIL_ATTRIBUTE, inbox.getEmail());
        context.setAttribute(TENANTS_ATTRIBUTE, inbox.getDatasources());
    }

    @Override
    public void run() {
        try {
            getLogger().info("[{}] Completed", inbox.getEmail());
            final Session session = inbox.getSession();
            final SessionCommand command = commandFactory.getCommand(this, session);
            command.run();
        } catch (Exception e) {
            getLogger().error("Unable to process Inbox {}", inbox.getEmail(), e);
        } finally {
            getLogger().info("Done processing Inbox {}", inbox.getEmail());
        }

    }
}

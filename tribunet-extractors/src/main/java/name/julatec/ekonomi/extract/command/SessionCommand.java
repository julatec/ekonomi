package name.julatec.ekonomi.extract.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class SessionCommand extends BaseCommand<SessionCommand> {

    protected final Session session;

    public SessionCommand(Context<?> context, Session session) {
        super(context);
        this.session = session;
    }

    @Override
    public void run() {
        try {
            final Store store = this.session.getStore("imaps");
            final StoreCommand command = commandFactory.getCommand(this, store);
            command.run();
        } catch (NoSuchProviderException e) {
            getLogger().error("Provider error not handled.", e);
        }

    }
}

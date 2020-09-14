package name.julatec.ekonomi.extract.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class StoreCommand extends BaseCommand<StoreCommand> {

    protected final Store store;

    public StoreCommand(Context<?> context, Store store) {
        super(context);
        this.store = store;
        context.setAttribute("folder", "INBOX");
    }

    @Override
    public void run() {
        try (store) {
            store.connect();
            final Folder folder = this.store.getFolder("INBOX");
            final FolderCommand command = this.commandFactory.getCommand(this, folder);
            command.run();
        } catch (MessagingException e) {
            getLogger().error("[{}] Unable to open inbox", e, context.<String>getAttribute("email"));
        }

    }
}

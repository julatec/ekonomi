package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.security.Inbox;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.xml.bind.JAXBException;
import java.io.InputStream;

@Service
public class CommandFactory {

    private final ApplicationContext context;
    private final DocumentoCommandFactory documentoCommandFactory;

    public CommandFactory(
            ApplicationContext context,
            DocumentoCommandFactory documentoCommandFactory) {
        this.context = context;
        this.documentoCommandFactory = documentoCommandFactory;
    }

    public <P extends BaseCommand<P>> InboxCommand
    getCommand(BaseCommand<P> parentCommand, Inbox inbox) {
        return context.getBean(InboxCommand.class, parentCommand.context.<P>push(), inbox);
    }

    public <P extends BaseCommand<P>> FolderCommand
    getCommand(BaseCommand<P> parentCommand, Folder folder) {
        return context.getBean(FolderCommand.class, parentCommand.context.<P>push(), folder);
    }

    public <P extends BaseCommand<P>> StoreCommand
    getCommand(BaseCommand<P> parentCommand, Store store) {
        return context.getBean(StoreCommand.class, parentCommand.context.<P>push(), store);
    }

    public <P extends BaseCommand<P>> MessageCommand
    getCommand(BaseCommand<P> parentCommand, Message message) {
        return context.getBean(MessageCommand.class, parentCommand.context.<P>push(), message);
    }

    public <P extends BaseCommand<P>> SessionCommand
    getCommand(BaseCommand<P> parentCommand, Session session) {
        return context.getBean(SessionCommand.class, parentCommand.context.<P>push(), session);
    }

    public <P extends BaseCommand<P>> XmlAttachmentCommand
    getXmlCommand(BaseCommand<P> parentCommand, InputStream stream) {
        return context.getBean(XmlAttachmentCommand.class, parentCommand.context.<P>push(), stream);
    }

    public <P extends BaseCommand<P>> DocumentCommand<? extends DocumentCommand>
    getCommand(BaseCommand<P> parentCommand, Document document) throws JAXBException {
        return documentoCommandFactory.getCommand(parentCommand, document);
    }

    public <P extends BaseCommand<P>> ZipAttachmentCommand
    getZipCommand(BaseCommand<P> parentCommand, InputStream stream) {
        return context.getBean(ZipAttachmentCommand.class, parentCommand.context.<P>push(), stream);
    }
}

package name.julatec.ekonomi.extract.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static name.julatec.ekonomi.extract.command.InboxCommand.EMAIL_ATTRIBUTE;
import static name.julatec.ekonomi.extract.command.MessageCommand.MESSAGE_NUMBER_ATTRIBUTE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class XmlAttachmentCommand extends BaseCommand<XmlAttachmentCommand> {

    private static final DocumentBuilderFactory dbFactory;

    static {
        dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        dbFactory.setValidating(false);
    }

    public final InputStream stream;

    public XmlAttachmentCommand(Context<?> context, InputStream stream) {
        super(context);
        this.stream = stream;
    }

    @Override
    public void run() {
        try {
            final DocumentBuilder builder = dbFactory.newDocumentBuilder();
            final Document document = builder.parse(stream);
            final Runnable command = commandFactory.getCommand(this, document);
            if (command != null) {
                for (int i = 1; i < 10; i++) {
                    try {
                        command.run();
                        break;
                    } catch (Exception e) {
                        getLogger().error("[{}] Unable to execute: {}", context.getAttribute(EMAIL_ATTRIBUTE), command, e);
                        try {
                            Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                        } catch (InterruptedException ex) {
                            getLogger().error("InterruptedException trying to read xml", ex);
                        }
                        continue;
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            getLogger().error("[{}/{}]Error parsing document",
                    context.getAttribute(EMAIL_ATTRIBUTE),
                    context.getAttribute(MESSAGE_NUMBER_ATTRIBUTE), e);
        } catch (IOException e) {
            getLogger().error("[{}/{}]Error parsing document",
                    context.getAttribute(EMAIL_ATTRIBUTE),
                    context.getAttribute("messageNumber"), e);
        } catch (SAXException e) {
            getLogger().error("[{}/{}]Error parsing document",
                    context.getAttribute(EMAIL_ATTRIBUTE), context.getAttribute("messageNumber"), e);
        } catch (JAXBException e) {
            getLogger().error("[{}/{}]Error parsing document",
                    context.getAttribute(EMAIL_ATTRIBUTE), context.getAttribute("messageNumber"), e);
        } catch (Exception e) {
            getLogger().error("[{}/{}]Error parsing document",
                    context.getAttribute(EMAIL_ATTRIBUTE), context.getAttribute("messageNumber"), e);
        }

    }
}

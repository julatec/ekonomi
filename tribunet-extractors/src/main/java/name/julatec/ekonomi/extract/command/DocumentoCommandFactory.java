package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.*;
import name.julatec.ekonomi.tribunet.annotation.AdapterFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static name.julatec.ekonomi.extract.command.InboxCommand.EMAIL_ATTRIBUTE;
import static name.julatec.ekonomi.extract.command.MessageCommand.MESSAGE_NUMBER_ATTRIBUTE;


@Service
public class DocumentoCommandFactory {

    private static final Map<Class<?>, Class<? extends DocumentCommand>> commands;

    private final DocumentoAdapterService adapterService;

    static {
        final Map<Class<?>, Class<? extends DocumentCommand>> commandsMap = new HashMap<>();
        commandsMap.put(Factura.class, FacturaCommand.class);
        commandsMap.put(FacturaCompra.class, FacturaCompraCommand.class);
        commandsMap.put(FacturaExportacion.class, FacturaExportacionCommand.class);
        commandsMap.put(MensajeReceptor.class, MensajeReceptorCommand.class);
        commandsMap.put(MensajeHacienda.class, MensajeHaciendaCommand.class);
        commandsMap.put(NotaCredito.class, NotaCreditoCommand.class);
        commandsMap.put(NotaDebito.class, NotaDebitoCommand.class);
        commands = Collections.unmodifiableMap(commandsMap);
    }

    private final ApplicationContext context;

    public DocumentoCommandFactory(
            DocumentoAdapterService adapterService,
            ApplicationContext context) {
        this.adapterService = adapterService;
        this.context = context;
    }

    public <P extends BaseCommand<P>> DocumentCommand<?>
    getCommand(BaseCommand<P> parentCommand, Document document) throws JAXBException {
        final String namespace = document.getDocumentElement().getNamespaceURI();
        if (namespace != null && adapterService.supportedNamespaces().contains(namespace)) {
            try {
                final Optional<Object> documento = adapterService.adapt(document);
                if (documento.isEmpty()) {
                    parentCommand.context.logger.warn("[{}][{}] Unable to adapt namespace: {}",
                            parentCommand.context.getAttribute(EMAIL_ATTRIBUTE),
                            parentCommand.context.getAttribute(MESSAGE_NUMBER_ATTRIBUTE),
                            namespace);
                    return null;
                }
                for (Map.Entry<Class<?>, Class<? extends DocumentCommand>> entry : commands.entrySet()) {
                    if (entry.getKey().isInstance(documento.get())) {
                        DocumentCommand command = context.getBean(
                                entry.getValue(),
                                parentCommand.context.push(),
                                document, documento.get());
                        return command;
                    }
                }
            } catch (NullPointerException e) {
                final NodeList nodeList = document.getDocumentElement().getElementsByTagName("Clave");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    parentCommand.context.logger.warn("[{}][{}] Unable to parse: [{}]",
                            parentCommand.context.getAttribute(EMAIL_ATTRIBUTE),
                            parentCommand.context.getAttribute(MESSAGE_NUMBER_ATTRIBUTE),
                            node.getTextContent());
                }
                parentCommand.context.logger.error("[{}][{}] Unable to adapt document",
                        parentCommand.context.getAttribute(EMAIL_ATTRIBUTE),
                        parentCommand.context.getAttribute(MESSAGE_NUMBER_ATTRIBUTE),
                        e);
                return null;
            }

        }
        parentCommand.context.logger.warn("[{}][{}] Unsupported namespace: {}",
                parentCommand.context.getAttribute(EMAIL_ATTRIBUTE),
                parentCommand.context.getAttribute(MESSAGE_NUMBER_ATTRIBUTE),
                namespace);
        return null;
    }


}

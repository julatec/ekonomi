package name.julatec.ekonomi.extract.command;

import jakarta.xml.bind.JAXBException;
import name.julatec.ekonomi.tribunet.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
        // `debug` y no `warn`: esto se escribe una vez por cada documento que entra bien, o sea
        // 9.256 líneas el 5 de setiembre de 2026 entre este mensaje y el de «Adapted». Un WARN
        // que se emite en el camino feliz no avisa de nada y ademas entierra a los que sí: ese
        // día producción se cayó a las 20:05 y nadie lo vio, con el error dentro de un
        // catalina.out de 17,9 MB. El único de los tres que se queda en WARN es el de abajo, el
        // de no poder adaptar, que es el que señala un comprobante perdido.
        parentCommand.context.logger.debug("[{}][{}] Adapting namespace: {}",
                parentCommand.context.getAttribute(EMAIL_ATTRIBUTE),
                parentCommand.context.getAttribute(MESSAGE_NUMBER_ATTRIBUTE),
                namespace);
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
                parentCommand.context.logger.debug("[{}][{}] Adapted namespace: {}",
                        parentCommand.context.getAttribute(EMAIL_ATTRIBUTE),
                        parentCommand.context.getAttribute(MESSAGE_NUMBER_ATTRIBUTE),
                        namespace);

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

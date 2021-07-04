package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.AdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

@Service
public class DocumentoAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoAdapterService.class);

    private Map<String, DocumentLifecycle> lifecycleMap;

    private final DocumentBuilderFactory factory;

    public DocumentoAdapterService() {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
    }

    public Optional<Documento> adapt(InputStream xml, Consumer<Throwable> throwableConsumer) {
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(xml);
            return getDocumento(document);
        } catch (ParserConfigurationException | SAXException | JAXBException | IOException e) {
            throwableConsumer.accept(e);
        }
        return Optional.empty();
    }

    public Optional<Documento> adapt(String xml, Consumer<Throwable> throwableConsumer) {
        return adapt(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), throwableConsumer);
    }

    public Optional<Documento> getDocumento(Document document) throws JAXBException {
        return adapt(document)
                .flatMap(o -> Optional.ofNullable(o instanceof Documento ? (Documento) o : null));
    }

    public Optional<Object> adapt(Document document) throws JAXBException {
        final String namespace = document.getDocumentElement().getNamespaceURI();
        try {
            if (namespace != null && lifecycleMap.containsKey(namespace)) {
                final DocumentLifecycle lifecycle = lifecycleMap.get(namespace);
                final Object entity = lifecycle.unmarshaller.unmarshal(document);
                final Object adaptedEntity = lifecycle.adapter.adapt(entity);
                return Optional.ofNullable(adaptedEntity);
            }
            return Optional.empty();
        } catch (NullPointerException e) {
            logger.error("Parsing document", e);
            return Optional.empty();
        }

    }


    public Set<String> supportedNamespaces() {
        return lifecycleMap.keySet();
    }

    @Autowired
    private void reportFactories(ListableBeanFactory factory) throws JAXBException {
        final Map<String, DocumentLifecycle> lifecycleMap = new TreeMap<>();
        for (AdapterFactory adapter : factory.getBeansOfType(AdapterFactory.class).values()) {
            for (Class<?> target : adapter.supportedClasses()) {
                final XmlRootElement rootElement = target.getAnnotation(XmlRootElement.class);
                if (rootElement == null) {
                    continue;
                }
                final String namespace = rootElement.namespace();
                lifecycleMap.put(namespace, new DocumentLifecycle(adapter, target));
            }
        }
        this.lifecycleMap = Collections.unmodifiableMap(lifecycleMap);
    }

    private static final class DocumentLifecycle {

        final AdapterFactory adapter;
        final Class<?> document;
        final JAXBContext jaxbContext;
        final Unmarshaller unmarshaller;
        final Marshaller marshaller;

        private DocumentLifecycle(AdapterFactory adapter, Class<?> document) throws JAXBException {
            this.adapter = adapter;
            this.document = document;
            this.jaxbContext = JAXBContext.newInstance(document.getPackageName());
            this.unmarshaller = jaxbContext.createUnmarshaller();
            this.marshaller = jaxbContext.createMarshaller();
            this.unmarshaller.setEventHandler(new CustomValidationEventHandler());
        }

        private static class CustomValidationEventHandler implements ValidationEventHandler {

            public boolean handleEvent(ValidationEvent evt) {
                if (evt.getMessage().contains("Unexpected element") ||
                        evt.getMessage().contains("elemento inesperado")) {
                    return true;
                }
                return true;
            }
        }
    }
}

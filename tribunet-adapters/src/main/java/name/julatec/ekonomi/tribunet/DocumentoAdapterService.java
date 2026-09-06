package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.AdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.annotation.XmlRootElement;
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
public class DocumentoAdapterService implements ErrorHandler {


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
            builder.setErrorHandler(this);
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
                final Object entity = lifecycle.nuevoUnmarshaller().unmarshal(document);
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

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        logger.warn(exception.getMessage(), exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        logger.warn(exception.getMessage(), exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        logger.warn(exception.getMessage(), exception);
    }

    private static final class DocumentLifecycle {

        final AdapterFactory adapter;
        final Class<?> document;

        /**
         * Compartido a propósito: {@code JAXBContext} SÍ es thread-safe y construirlo es lo
         * caro. Lo que no se comparte es el {@code Unmarshaller}.
         */
        final JAXBContext jaxbContext;

        private DocumentLifecycle(AdapterFactory adapter, Class<?> document) throws JAXBException {
            this.adapter = adapter;
            this.document = document;
            this.jaxbContext = JAXBContext.newInstance(document.getPackageName());
        }

        /**
         * Uno nuevo por conversión, y no un campo compartido.
         * <p>
         * Antes era un campo. Este servicio es un {@code @Service} —singleton— y la ingesta lo
         * llama desde {@code ExtractExecutor}, que arranca con {@code min(8, núcleos)} hilos:
         * cuatro en el Pi de producción. La especificación de JAXB declara que un
         * {@code Unmarshaller} <b>no</b> es reentrante, y compartirlo corrompía su máquina de
         * estados interna.
         * <p>
         * En producción se veía como <b>125 «Unable to adapt» repartidos parejo entre los
         * cuatro hilos</b> (33/29/35/28). Ese reparto uniforme es la firma del estado mutable
         * compartido: unos pocos documentos rotos se concentrarían en quien los procesó. Cada
         * uno de esos fallos es un comprobante que llegó por correo y no se guardó.
         * <p>
         * 🔴 Y el modo de falla era peor que un fallo: JAXB lanza {@link AssertionError} desde
         * {@code UnmarshallingContext$State.pop}, que es un {@code Error} y NO lo atrapa el
         * {@code catch} de {@link #adapt(InputStream, Consumer)} —que solo lista excepciones
         * comprobadas—, así que se llevaba puesta la tarea de extracción entera y no solo el
         * documento. En los logs eso es «Tarea de extracción terminada con error».
         * <p>
         * Crearlo por llamada es barato: lo caro es el {@code JAXBContext}, que se comparte.
         */
        Unmarshaller nuevoUnmarshaller() throws JAXBException {
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(new CustomValidationEventHandler());
            return unmarshaller;
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

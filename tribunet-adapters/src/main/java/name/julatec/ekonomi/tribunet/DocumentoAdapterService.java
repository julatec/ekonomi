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
            final byte[] bytes = xml.readAllBytes();
            try {
                return getDocumento(parsear(bytes));
            } catch (SAXException primerIntento) {
                // Hay comprobantes que llegan con basura pegada DESPUÉS del cierre de la raíz
                // —otro documento concatenado, una firma suelta, relleno del servidor de
                // correo—. Xerces corta con «Content is not allowed in trailing section» y el
                // comprobante entero se pierde, aunque la parte válida esté completa y bien
                // formada. En producción eso dejó un mensaje reintentando cuatro veces al día
                // desde el 30 set 2026 sin entrar nunca.
                //
                // Se reintenta UNA vez recortando a partir del cierre de la raíz. Si el
                // recorte no cambia nada, o si el segundo intento también falla, se propaga
                // el error ORIGINAL: el log tiene que mostrar el problema real y no uno
                // derivado del salvamento.
                final byte[] recortado = recortarTrasElCierreDeLaRaiz(bytes);
                if (recortado == null) {
                    throw primerIntento;
                }
                try {
                    final Optional<Documento> documento = getDocumento(parsear(recortado));
                    logger.warn("Documento aceptado tras descartar {} bytes pegados después del " +
                                    "cierre de la raíz. El original no era XML válido: {}",
                            bytes.length - recortado.length, primerIntento.getMessage());
                    return documento;
                } catch (SAXException | JAXBException segundoIntento) {
                    primerIntento.addSuppressed(segundoIntento);
                    throw primerIntento;
                }
            }
        } catch (ParserConfigurationException | SAXException | JAXBException | IOException e) {
            throwableConsumer.accept(e);
        }
        return Optional.empty();
    }

    private Document parsear(byte[] bytes) throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(this);
        return builder.parse(new ByteArrayInputStream(bytes));
    }

    /**
     * Recorta lo que venga después del primer cierre del elemento raíz.
     * <p>
     * Trabaja sobre los BYTES y no sobre un {@code String}: el comprobante declara su propia
     * codificación y decodificarlo para volver a codificarlo lo corrompería. Los nombres de
     * elemento de los esquemas de Hacienda son ASCII, así que la secuencia {@code </Raiz>}
     * son los mismos bytes en UTF-8 y en ISO-8859-1 — basta con truncar el arreglo.
     * <p>
     * Se corta en el PRIMER cierre, no en el último: si lo que viene pegado es otro documento
     * con la misma raíz, cortar al final dejaría los dos y seguiría sin ser válido.
     *
     * @return el arreglo recortado, o {@code null} si no hay nada que recortar.
     */
    static byte[] recortarTrasElCierreDeLaRaiz(byte[] bytes) {
        final String raiz = nombreDeLaRaiz(bytes);
        if (raiz == null) {
            return null;
        }
        final byte[] cierre = ("</" + raiz + ">").getBytes(StandardCharsets.US_ASCII);
        final int desde = indiceDe(bytes, cierre, 0);
        if (desde < 0) {
            return null;
        }
        final int corte = desde + cierre.length;
        if (corte >= bytes.length || soloEspaciosDesde(bytes, corte)) {
            return null; // No sobra nada: el error es otro y recortar no ayudaría.
        }
        return Arrays.copyOf(bytes, corte);
    }

    /** Nombre del elemento raíz, saltándose prólogo, comentarios y DOCTYPE. */
    private static String nombreDeLaRaiz(byte[] bytes) {
        int i = 0;
        while (i < bytes.length) {
            if (bytes[i] != '<') {
                i++;
                continue;
            }
            if (i + 1 < bytes.length && (bytes[i + 1] == '?' || bytes[i + 1] == '!')) {
                i++; // Prólogo, comentario o DOCTYPE: no es la raíz.
                continue;
            }
            final int inicio = i + 1;
            int fin = inicio;
            while (fin < bytes.length && !esDelimitadorDeNombre(bytes[fin])) {
                fin++;
            }
            return fin > inicio ? new String(bytes, inicio, fin - inicio, StandardCharsets.US_ASCII) : null;
        }
        return null;
    }

    private static boolean esDelimitadorDeNombre(byte b) {
        return b == '>' || b == '/' || b == ' ' || b == '\t' || b == '\r' || b == '\n';
    }

    private static boolean soloEspaciosDesde(byte[] bytes, int desde) {
        for (int i = desde; i < bytes.length; i++) {
            final byte b = bytes[i];
            if (b != ' ' && b != '\t' && b != '\r' && b != '\n' && b != 0) {
                return false;
            }
        }
        return true;
    }

    private static int indiceDe(byte[] heno, byte[] aguja, int desde) {
        outer:
        for (int i = desde; i <= heno.length - aguja.length; i++) {
            for (int j = 0; j < aguja.length; j++) {
                if (heno[i + j] != aguja[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
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

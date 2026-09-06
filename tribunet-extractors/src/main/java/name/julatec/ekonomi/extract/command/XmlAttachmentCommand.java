package name.julatec.ekonomi.extract.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
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

    /**
     * Ve si el InputStream arranca con {@code '<'} (saltando un BOM UTF-8 y espacios en
     * blanco), sin consumirlo: usa {@code mark}/{@code reset} para dejarlo intacto.
     * <p>
     * Existe porque {@code MessageCommand.processPart} le manda a este comando cualquier
     * adjunto que no supo identificar por content-type o extensión —esa es la rama que
     * produce, contra el buzón real, los {@code SAXParseException} con "Content is not
     * allowed in prolog" en línea 1 columna 1—. El caso encontrado el 6 sep 2026: el
     * remitente codifica el nombre del adjunto como <i>encoded-word</i> RFC 2047 dentro del
     * parámetro {@code filename} (en vez del RFC 2231 correcto), y
     * {@code MimeBodyPart.getFileName()} no lo decodifica, así que ni {@code .endsWith(".pdf")}
     * ni {@code .endsWith(".xml")} matchean y el PDF adjunto termina aquí. No hay ningún
     * comprobante que se pierda —los adjuntos XML de ese mismo correo sí son XML y parsean
     * bien—, así que basta con no intentar parsear lo que ya se ve que no es XML, en vez de
     * dejar que reviente el DocumentBuilder y loguee un ERROR por nada.
     */
    private boolean pareceXml(InputStream stream) throws IOException {
        stream.mark(64);
        try {
            int b = stream.read();
            if (b == 0xEF) {
                stream.read(); // BB
                stream.read(); // BF
                b = stream.read();
            }
            while (b == ' ' || b == '\t' || b == '\r' || b == '\n') {
                b = stream.read();
            }
            return b == '<';
        } finally {
            stream.reset();
        }
    }

    @Override
    public void run() {
        try {
            final BufferedInputStream contenido = new BufferedInputStream(stream);
            if (!pareceXml(contenido)) {
                getLogger().trace("[{}/{}] Adjunto descartado sin parsear: no arranca con '<'",
                        context.getAttribute(EMAIL_ATTRIBUTE), context.getAttribute(MESSAGE_NUMBER_ATTRIBUTE));
                return;
            }
            final DocumentBuilder builder = dbFactory.newDocumentBuilder();
            final Document document = builder.parse(contenido);
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
            // "Invalid byte N of M-byte UTF-8 sequence" en medio de un documento (no en el
            // prólogo) NO es este bug: es un adjunto que sí arranca con '<' y ya pasó el peek
            // de arriba. Investigado contra un _respuesta_hacienda.xml real el 6 sep 2026: el
            // documento declara encoding="UTF-8" pero en el byte que revienta el parseo hay un
            // 0xF3 exactamente donde el texto dice "cantón" — 0xF3 es 'ó' en Latin-1/
            // Windows-1252, no un byte UTF-8 válido ahí. O sea, es a Hacienda (o a un relé
            // intermedio) al que se le mezclaron los encodings al generar ese MensajeHacienda,
            // no algo que rompamos nosotros leyendo el InputStream. No hay forma de saber desde
            // acá si TODO el documento es Latin-1 o solo ese tramo, así que no se reintenta con
            // otro Charset a ciegas: eso arriesga guardar una factura con texto mal decodificado
            // sin que nadie lo note. Se deja como error genuino, tal como está.
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

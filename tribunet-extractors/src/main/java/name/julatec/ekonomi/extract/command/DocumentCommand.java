package name.julatec.ekonomi.extract.command;

import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public abstract class DocumentCommand<T extends DocumentCommand<T>> extends BaseCommand<T> {

    protected final Document document;

    protected DocumentCommand(Context<?> context, Document document) {
        super(context);
        this.document = document;
    }

    protected Stream<String> getTenants() {
        return context.<Set<String>>getAttribute(InboxCommand.TENANTS_ATTRIBUTE)
                .stream()
                .flatMap(Collection::stream);
    }

    protected String getStringFromDocument(Document document) {
        try {
            final DOMSource domSource = new DOMSource(document);
            final StringWriter writer = new StringWriter();
            final StreamResult result = new StreamResult(writer);
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException e) {
            getLogger().error("Unable to serialize document", e);
            return null;
        }
    }

    protected final String getStringFromDocument() {
        return getStringFromDocument(this.document);
    }
}

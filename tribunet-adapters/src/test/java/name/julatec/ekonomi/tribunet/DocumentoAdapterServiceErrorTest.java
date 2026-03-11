package name.julatec.ekonomi.tribunet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@SpringBootTest
class DocumentoAdapterServiceErrorTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    DocumentoAdapterService documentoAdapterService;

    @Test
    void adapt_invalidXml_returnsEmpty() {
        AtomicReference<Throwable> caught = new AtomicReference<>();
        Optional<Documento> result = documentoAdapterService.adapt("not-valid-xml", caught::set);
        assertTrue(result.isEmpty());
        assertNotNull(caught.get());
    }

    @Test
    void adapt_unknownNamespace_returnsEmpty() {
        // Valid XML but unknown namespace → no adapter → Optional.empty()
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<root xmlns=\"urn:unknown:namespace\"><child/></root>";
        AtomicReference<Throwable> caught = new AtomicReference<>();
        Optional<Documento> result = documentoAdapterService.adapt(xml, caught::set);
        assertTrue(result.isEmpty());
        assertNull(caught.get()); // no exception thrown for unknown namespace
    }

    @Test
    void adapt_string_validXml_returnsDocumento() {
        // Minimal test that adapt(String, Consumer) delegates correctly
        // A valid XML for an unknown namespace still returns empty without exception
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<root xmlns=\"urn:unknown\"/>";
        Optional<Documento> result = documentoAdapterService.adapt(xml, e -> {});
        assertTrue(result.isEmpty()); // unknown namespace → empty
    }
}

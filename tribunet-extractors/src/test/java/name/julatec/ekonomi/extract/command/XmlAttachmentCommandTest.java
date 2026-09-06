package name.julatec.ekonomi.extract.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

/**
 * Dos {@code SAXParseException} que produce el extractor de facturas.tribuconta@julatec.name,
 * investigadas contra el buzón real el 6 sep 2026, y que resultaron ser problemas distintos.
 *
 * <p><b>"Content is not allowed in prolog" (línea 1, columna 1).</b> No es {@code text/plain}
 * cayendo en el switch de {@code MessageCommand.processPart} agrupado con los tipos XML —esa era
 * la sospecha inicial, pero los stack traces de producción apuntan siempre a
 * {@code MessageCommand.java:105}, la rama {@code application/octet-stream} de "tipo no
 * soportado, pero se intenta igual". La causa real: el remitente pone el nombre de archivo del
 * adjunto codificado como <i>encoded-word</i> RFC 2047 dentro del parámetro
 * {@code filename} de {@code Content-Disposition} (p. ej.
 * {@code filename="=?UTF-8?B?RmFjdHVyYUVsZWN0cm9uaWNhLnBkZg==?="} en vez del RFC 2231 correcto),
 * y {@code MimeBodyPart.getFileName()} de jakarta.mail no lo decodifica —se confirmó pidiéndole
 * ese mensaje real al buzón: la cadena que llega a {@code processPart} termina en {@code "==?="},
 * no en {@code ".pdf"}. El adjunto de factura cae entonces a la rama que prueba parsear
 * cualquier cosa como XML, y cuando ese "cualquier cosa" es el PDF adjunto (empieza con
 * {@code %PDF-1.7}), el parser truena en la primera columna. Los adjuntos XML del mismo correo
 * pasan por la misma rama sin problema —parsean bien igual—, así que no se pierde ningún
 * comprobante: es puro ruido de log a nivel ERROR por algo que nunca fue una factura.
 *
 * <p><b>"Invalid byte 2 of 4-byte UTF-8 sequence" (línea 14, columna 79).</b> Esto sí es
 * corrupción real, pero no del lado nuestro: en el adjunto {@code _respuesta_hacienda.xml} de un
 * mensaje real, el byte en cuestión es {@code 0xF3} exactamente donde el texto dice
 * "...cantón y distrito...". {@code 0xF3} es el código de {@code 'ó'} en Latin-1/Windows-1252, no
 * en UTF-8 —y el documento declara {@code encoding="UTF-8"} en el prólogo—. O sea: Hacienda (o
 * algún relé intermedio) generó ese {@code MensajeHacienda} con bytes Latin-1 pero la etiqueta de
 * encoding dice UTF-8. No hay forma de adivinar con certeza si el resto del documento es Latin-1
 * completo o solo ese tramo, así que no se intenta una recodificación alternativa sin más
 * evidencia: se documenta acá y se sigue logueando como error, que es lo correcto para algo que
 * de verdad es un documento inválido.
 */
class XmlAttachmentCommandTest {

    private XmlAttachmentCommand comando(Logger logger, byte[] contenido) {
        final Context<XmlAttachmentCommand> context = new Context<>(null, logger);
        final XmlAttachmentCommand comando =
                new XmlAttachmentCommand(context, new ByteArrayInputStream(contenido));
        // commandFactory normalmente lo pone Spring (@Autowired). Acá no hay contenedor, y un
        // XML válido sí llega a usarlo (para despachar el Documento); un mock que responde null
        // alcanza porque el test solo verifica el log, no qué comando de dominio se ejecuta
        // después.
        comando.commandFactory = mock(CommandFactory.class);
        return comando;
    }

    private long invocacionesDe(Logger logger, String metodo) {
        return mockingDetails(logger).getInvocations().stream()
                .filter(invocacion -> invocacion.getMethod().getName().equals(metodo))
                .count();
    }

    private byte[] concatenar(byte[]... partes) {
        final ByteArrayOutputStream salida = new ByteArrayOutputStream();
        for (byte[] parte : partes) {
            salida.writeBytes(parte);
        }
        return salida.toByteArray();
    }

    @Test
    @DisplayName("un adjunto que no arranca con '<' (el PDF que cayó en la rama de "
            + "octet-stream) no se intenta parsear como XML, y no ensucia el log con ERROR")
    void adjuntoQueNoEsXmlNoLogueaError() {
        final Logger logger = mock(Logger.class);
        // Cabecera real de un PDF (%PDF-1.7...), tal como llega cuando MessageCommand cae a la
        // rama "Unsupported file type, but trying" y le pasa el InputStream del adjunto crudo al
        // mismo XmlAttachmentCommand que procesa las facturas.
        final byte[] pdf = concatenar(
                "%PDF-1.7\r%".getBytes(StandardCharsets.ISO_8859_1),
                new byte[]{(byte) 0xC8, (byte) 0xC8, (byte) 0xC8, (byte) 0xC8},
                "\r\n1 0 obj\n<</Title()/Producer(Aspose.PDF)>>\n".getBytes(StandardCharsets.ISO_8859_1)
        );

        comando(logger, pdf).run();

        assertEquals(0, invocacionesDe(logger, "error"),
                "un PDF no debería producir ningún log de error: nunca fue una factura");
    }

    @Test
    @DisplayName("un XML real con un byte inválido a mitad de documento sigue "
            + "logueando error: es corrupción genuina, no ruido de tipo de adjunto")
    void corrupcionRealDeUnXmlSigueSiendoError() {
        final Logger logger = mock(Logger.class);
        // Reproduce el hallazgo real: el documento arranca perfectamente como XML declarado
        // UTF-8, pero en medio del texto aparece 0xF3 —'ó' en Latin-1— seguido de un byte ASCII
        // que no es una continuación UTF-8 válida. Xerces lo reporta como
        // "Invalid byte 2 of 4-byte UTF-8 sequence", igual que en producción.
        final byte[] xmlCorrupto = concatenar(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MensajeHacienda>cant"
                        .getBytes(StandardCharsets.US_ASCII),
                new byte[]{(byte) 0xF3, 'n'},
                " y distrito</MensajeHacienda>".getBytes(StandardCharsets.US_ASCII)
        );

        comando(logger, xmlCorrupto).run();

        assertEquals(1, invocacionesDe(logger, "error"),
                "un XML de verdad corrupto sí debe quedar registrado como error");
    }

    @Test
    @DisplayName("un XML válido, como llegan de verdad los adjuntos de Hacienda "
            + "(arrancando directo en '<?xml'), se sigue parseando igual que antes")
    void xmlValidoSigueParseandoseSinError() {
        final Logger logger = mock(Logger.class);
        final byte[] xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Vacio/>"
                .getBytes(StandardCharsets.UTF_8);

        comando(logger, xml).run();

        assertEquals(0, invocacionesDe(logger, "error"),
                "un documento bien formado no debe fallar ni loguear error");
    }

    @Test
    @DisplayName("un XML válido con BOM UTF-8 adelante (algunos remitentes lo agregan) "
            + "también se sigue parseando: el peek lo salta, no lo confunde con 'no es XML'")
    void xmlConBomSigueParseandoseSinError() {
        final Logger logger = mock(Logger.class);
        final byte[] xml = concatenar(
                new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Vacio/>".getBytes(StandardCharsets.UTF_8)
        );

        comando(logger, xml).run();

        assertEquals(0, invocacionesDe(logger, "error"),
                "el BOM no es contenido: no debería hacer que el peek descarte un XML válido");
    }
}

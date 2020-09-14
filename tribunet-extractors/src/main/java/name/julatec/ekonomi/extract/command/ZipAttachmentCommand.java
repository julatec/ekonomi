package name.julatec.ekonomi.extract.command;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class ZipAttachmentCommand extends BaseCommand<ZipAttachmentCommand> {

    private final InputStream inputStream;

    public ZipAttachmentCommand(Context<?> context, InputStream inputStream) {
        super(context);
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try {
            final ZipInputStream stream = new ZipInputStream(this.inputStream);
            for (ZipEntry entry = stream.getNextEntry(); entry != null; entry = stream.getNextEntry()) {
                final byte[] bytesIn = new byte[1000];
                if (entry.isDirectory()) {
                    continue;
                }
                if (entry.getName().toLowerCase().endsWith(".xml")) {
                    final InputStream inputStream = convertZipInputStreamToInputStream(stream);
                    XmlAttachmentCommand command = commandFactory.getXmlCommand(this, inputStream);
                    command.run();
                    continue;
                }
                while (stream.read(bytesIn) != -1) ;
            }
        } catch (IOException | IllegalArgumentException e) {
            getLogger().error("Error reading zip file", e);
        }
    }

    private InputStream convertZipInputStreamToInputStream(final ZipInputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        final InputStream is = new ByteArrayInputStream(out.toByteArray());
        return is;
    }
}

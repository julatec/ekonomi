package name.julatec.ekonomi.extract.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class MessageCommand extends BaseCommand<MessageCommand> {

    public static final String MESSAGE_NUMBER_ATTRIBUTE = "messageNumber";

    protected final Message message;

    public MessageCommand(Context<?> context, Message message) {
        super(context);
        this.message = message;
        context.setAttribute(MESSAGE_NUMBER_ATTRIBUTE, message.getMessageNumber());
    }

    private void processMultipart(Multipart multipart) throws IOException, MessagingException {
        for (int i = 0; i < multipart.getCount(); i++) {
            final MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                processPart(part);
            } else if (part.isMimeType("multipart/*")) {
                processMultipart((Multipart) part.getContent());
            } else {
                processPart(part);
            }
        }
    }

    @Override
    public void run() {
        try {
            final Object content = this.message.getContent();
            if (content instanceof Multipart) {
                processMultipart((Multipart) content);
            }
        } catch (MessagingException e) {
            getLogger().error("Unable to read message {}/{}/{}",
                    message.getFolder().getStore(),
                    message.getFolder(),
                    message.getMessageNumber());
        } catch (IOException e) {
            getLogger().error("Unable to read message {}/{}/{}",
                    message.getFolder().getStore(),
                    message.getFolder(),
                    message.getMessageNumber());
        }
    }

    private void processPart(MimeBodyPart part) throws MessagingException, IOException {
        if (part.getFileName() == null) {
            return;
        }
        final String filename = part.getFileName().toLowerCase();
        switch (part.getContentType().split(";")[0].toLowerCase()) {
            case "application/xml":
            case "application/xhtml+xml":
            case "text/xhtml+xml":
            case "text/xml":
            case "text/plain": {
                final XmlAttachmentCommand command =
                        commandFactory.getXmlCommand(this, part.getInputStream());
                command.run();
                break;
            }
            case "application/x-zip-compressed":
            case "application/zip": {
                final ZipAttachmentCommand command =
                        commandFactory.getZipCommand(this, part.getInputStream());
                command.run();
                break;
            }
            case "application/octet-stream": {
                if (filename.endsWith(".zip")) {
                    final ZipAttachmentCommand command =
                            commandFactory.getZipCommand(this, part.getInputStream());
                    command.run();
                } else if (filename.endsWith(".xml")) {
                    final XmlAttachmentCommand command =
                            commandFactory.getXmlCommand(this, part.getInputStream());
                    command.run();
                    break;
                } else if (filename.endsWith(".pdf")) {
                    break;
                } else {
                    getLogger().trace("Unsupported file type [{}/{}], but trying.", part.getContentType(), filename);
                    try {
                        final XmlAttachmentCommand command =
                                commandFactory.getXmlCommand(this, part.getInputStream());
                        command.run();
                    } catch (Exception e) {
                        getLogger().trace("Unsupported file type [{}/{}], failed.", part.getContentType(), filename, e);
                    }
                    break;
                }
                break;
            }
            case "image/jpeg":
            case "application/vnd.ms-excel":
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            case "application/pdf":
                break;
            default:
                if (filename.endsWith(".xml")) {
                    final XmlAttachmentCommand command =
                            commandFactory.getXmlCommand(this, part.getInputStream());
                    command.run();
                    break;
                } else if (filename.endsWith(".pdf") || filename.endsWith(".pdf?=")) {
                    break;
                } else if (filename.endsWith(".png") || filename.endsWith(".jpg")) {
                    break;
                } else if (filename.endsWith(".html") || filename.endsWith(".json")) {
                    break;
                }
                getLogger().trace("Unsupported file type [{}/{}]", part.getContentType(), filename);
                break;
        }
    }
}

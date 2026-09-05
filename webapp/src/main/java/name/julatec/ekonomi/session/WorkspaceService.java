package name.julatec.ekonomi.session;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import name.julatec.ekonomi.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.format;

@Service
public class WorkspaceService {

    private static final String APP_PREFIX = "app";

    final ObjectMapper objectMapper = new ObjectMapper();

    private WorkspaceFactory factory;

    private ResourceBundleMessageSource messages;

    public void printSession(Workspace workspace, PrintWriter writer, Locale locale) throws JacksonException {
        writer.print("const session = ");
        writer.print(objectMapper.writeValueAsString(workspace.getSession()));
        writer.println(";");
        final Object[] empty = new Object[0];
        Map<String, Object> messages = new TreeMap<>();
        for (Message messageName : Message.values()) {
            final String message = this.messages.getMessage(format("%s.%s", APP_PREFIX, messageName.name()), empty, locale);
            messages.put(messageName.name(), message);
        }
        writer.print("const app = ");
        writer.print(objectMapper.writeValueAsString(messages));
        writer.println(";");
    }

    public Workspace getWorkspace(Authentication authentication, HttpServletRequest request) {
        return factory.getWorkspace(authentication, request);
    }

    /**
     * Los mismos mensajes que {@link #printSession} inyectaba como {@code const app = {...}},
     * pero como mapa: la API los devuelve dentro del JSON en vez de servir JavaScript.
     */
    public Map<String, Object> getMessages(Locale locale) {
        final Object[] empty = new Object[0];
        final Map<String, Object> mensajes = new TreeMap<>();
        for (Message messageName : Message.values()) {
            mensajes.put(messageName.name(),
                    this.messages.getMessage(format("%s.%s", APP_PREFIX, messageName.name()), empty, locale));
        }
        return mensajes;
    }

    @Autowired
    WorkspaceService setFactory(WorkspaceFactory factory) {
        this.factory = factory;
        return this;
    }

    @Autowired
    WorkspaceService setMessages(@Qualifier(AppConfig.APP_MESSAGES) ResourceBundleMessageSource messages) {
        this.messages = messages;
        return this;
    }

    private enum Message {
        hello,
        title,
        reports,
        purchases,
        sales,
        downloadReports,
        dashboard,
        searchFor,
        transactions,
        tenant
    }


}

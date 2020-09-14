package name.julatec.ekonomi.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import name.julatec.ekonomi.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
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

    public void printSession(Workspace workspace, PrintWriter writer, Locale locale) throws JsonProcessingException {
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

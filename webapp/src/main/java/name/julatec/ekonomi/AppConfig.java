package name.julatec.ekonomi;

import name.julatec.ekonomi.report.ReportController;
import name.julatec.ekonomi.security.AuthenticationService;
import name.julatec.ekonomi.session.Workspace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;

@Configuration
// Las clases son marcadores de paquete, no beans: cada una nombra el paquete que hay que
// escanear. `Workspace` reemplazó acá a `WorkspaceController` cuando ese controlador se
// eliminó —servía `/js/session.js`, que devolvía JavaScript literal y lo sustituyó
// `GET /api/session`—; el paquete que marcaba sigue siendo el mismo.
@ComponentScan(basePackageClasses = {
        ReportController.class,
        AuthenticationService.class,
        Workspace.class
})
public class AppConfig {

    public static final String APP_MESSAGES = "appMessages";
    public static final String MESSAGES = "templateMessages";

    @Bean(name = APP_MESSAGES)
    public ResourceBundleMessageSource getMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages/index");
        messageSource.setDefaultEncoding(StandardCharsets.ISO_8859_1.name());
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean(name = MESSAGES)
    public ResourceBundleMessageSource getMessages() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages/templates");
        messageSource.setDefaultEncoding(StandardCharsets.ISO_8859_1.name());
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

}
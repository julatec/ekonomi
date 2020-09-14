package name.julatec.ekonomi;

import name.julatec.ekonomi.report.ReportController;
import name.julatec.ekonomi.security.AuthenticationService;
import name.julatec.ekonomi.session.Workspace;
import name.julatec.ekonomi.session.WorkspaceController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;

@Configuration
@ComponentScan(basePackageClasses = {
        ReportController.class,
        AuthenticationService.class,
        WorkspaceController.class
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
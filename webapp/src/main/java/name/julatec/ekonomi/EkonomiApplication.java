package name.julatec.ekonomi;

//import cr.co.agropag.lang.tex.BookWritingService;
//import org.jpl7.JPL;
import name.julatec.ekonomi.security.AuthenticationService;
import name.julatec.ekonomi.storage.SecurityConfig;
import name.julatec.ekonomi.storage.StorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        }

)
@Import({
        SecurityConfig.class,
        StorageConfig.class,
        AppConfig.class
})
@EnableWebSecurity
@EnableScheduling
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class EkonomiApplication extends WebSecurityConfigurerAdapter {


    AuthenticationService authenticationUserDetailsService;

    static {
        //JPL.loadNativeLibrary();
    }

    public static void main(String[] args) {
        System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.CommonsLogger" );
        SpringApplication.run(EkonomiApplication.class, args);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .x509()
                .userDetailsService(userDetailsService())
                .authenticationUserDetailsService(authenticationUserDetailsService);
    }

    @Autowired
    EkonomiApplication setAuthenticationUserDetailsService(
            AuthenticationService authenticationUserDetailsService) {
        this.authenticationUserDetailsService = authenticationUserDetailsService;
        return this;
    }
}

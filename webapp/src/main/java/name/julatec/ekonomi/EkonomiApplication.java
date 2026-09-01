package name.julatec.ekonomi;

import name.julatec.ekonomi.security.AuthenticationService;
import name.julatec.ekonomi.storage.SecurityConfig;
import name.julatec.ekonomi.storage.StorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
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
@EnableWebSecurity(debug = false)
@EnableScheduling
//@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
// @EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class EkonomiApplication /*extends WebSecurityConfigurerAdapter*/ {


    AuthenticationService authenticationUserDetailsService;

    static {
        //JPL.loadNativeLibrary();
    }

    public static void main(String[] args) {
        System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.CommonsLogger");
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


    /**
     * Autenticación únicamente por certificado de firma digital.
     * <p>
     * Aquí estuvo {@code .httpBasic(...)}, que junto con la contraseña fija de
     * {@code User.getPassword()} permitía entrar conociendo solo una cédula
     * —dato público— desde internet. La aplicación nunca tuvo contraseñas
     * reales, así que retirar el mecanismo no le quita acceso a nadie.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .x509(x509 -> x509.authenticationUserDetailsService(this.authenticationUserDetailsService))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(this.authenticationUserDetailsService))
                .build();
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        return new InMemoryUserDetailsManager(
//                User.withUsername("julatec@agropag.co.cr")
//                        .password("") // no se usa
//                        .roles("USER")
//                        .build()
//        );
//    }

    @Autowired
    EkonomiApplication setAuthenticationUserDetailsService(
            AuthenticationService authenticationUserDetailsService) {
        this.authenticationUserDetailsService = authenticationUserDetailsService;
        return this;
    }
}

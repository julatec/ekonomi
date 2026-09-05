package name.julatec.ekonomi;

import name.julatec.ekonomi.security.AuthenticationService;
import name.julatec.ekonomi.storage.SecurityConfig;
import name.julatec.ekonomi.storage.StorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
        }

)
@Import({
        SecurityConfig.class,
        StorageConfig.class,
        AppConfig.class
})
@EnableWebSecurity(debug = false)
@EnableScheduling
@EnableMethodSecurity(securedEnabled = true)
public class EkonomiApplication {


    AuthenticationService authenticationUserDetailsService;

    /**
     * Orígenes permitidos para CORS.
     * <p>
     * Antes era {@code "*"} junto con {@code allowCredentials(true)}, combinación que los
     * navegadores rechazan de todas formas. Todo se sirve desde el mismo origen en producción;
     * esta lista existe para el dev server de Vite, que corre en otro puerto.
     */
    @Value("${name.julatec.ekonomi.cors.allowed-origins:}")
    List<String> allowedOrigins;

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
        configuration.setAllowedOrigins(allowedOrigins == null ? List.of() : allowedOrigins);
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

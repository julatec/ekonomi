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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
        }

)
@Import({
        SecurityConfig.class,
        StorageConfig.class,
        AppConfig.class,
        LocalDataSourceConfig.class,
        SchedulingConfig.class
})
@EnableWebSecurity(debug = false)
public class EkonomiApplication {


    AuthenticationService authenticationUserDetailsService;

    /**
     * Orígenes permitidos para CORS.
     * <p>
     * Antes era {@code "*"} junto con {@code allowCredentials(true)}, combinación que los
     * navegadores rechazan de todas formas. Todo se sirve desde el mismo origen en producción;
     * esta lista existe para el dev server de Vite, que corre en otro puerto.
     * <p>
     * En producción queda vacía y eso es lo correcto: ningún origen ajeno pasa. Con la lista
     * vacía el filtro sigue instalado, pero rechaza todo lo que venga de otro origen.
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

    /**
     * Con la lista vacía NO se registra ninguna regla, y esa condición es el arreglo de un
     * error medido el 5 sep 2026 contra la contabilidad real.
     * <p>
     * Vite emite los tags del bundle con el atributo {@code crossorigin}, y eso obliga al
     * navegador a mandar el encabezado {@code Origin} <b>aunque la petición sea del mismo
     * origen</b>. Spring trata como petición CORS a cualquiera que traiga ese encabezado, así
     * que con la lista vacía —el valor de producción— el filtro respondía <b>403 a los propios
     * assets de la aplicación</b> y la página quedaba en blanco:
     * <pre>
     *   GET /dist/assets/index-….js                 200
     *   GET /dist/assets/index-….js  con Origin     403
     * </pre>
     * Registrando la regla solo cuando hay orígenes configurados, producción se comporta como
     * antes de que existiera este bean —el filtro deja pasar— y el dev server de Vite sigue
     * teniendo su permiso donde la propiedad lo define.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            return source;
        }
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
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
                // Sin esta linea el bean `corsConfigurationSource` existe y nadie lo llama.
                // Estaba asi: la propiedad se documentaba como «para el dev server de Vite» y
                // no emitia un solo encabezado Access-Control-*. Hoy no se nota porque el
                // proxy de Vite hace que todo viaje al mismo origen, pero el dia que alguien
                // llame al API desde otro puerto va a buscar el error en el lugar equivocado.
                .cors(Customizer.withDefaults())
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

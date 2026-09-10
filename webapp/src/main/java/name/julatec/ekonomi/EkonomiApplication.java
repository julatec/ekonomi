package name.julatec.ekonomi;

import name.julatec.ekonomi.security.AuthenticationService;
import name.julatec.ekonomi.storage.SecurityConfig;
import name.julatec.ekonomi.storage.StorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
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
     * El propio origen SIEMPRE pasa; la lista solo agrega los ajenos.
     * <p>
     * Spring trata como petición CORS a cualquiera que traiga el encabezado {@code Origin}, y el
     * navegador lo manda <b>aunque la petición sea del mismo origen</b> en dos casos que esta
     * aplicación produce todo el tiempo: los tags que Vite emite con {@code crossorigin} para el
     * bundle, y cualquier {@code POST} de {@code fetch}. Con una lista de orígenes que no
     * incluyera el propio host, el filtro respondía <b>403 a los assets de la propia página</b>
     * y a {@code POST /api/chat}. Medido las dos veces: primero en producción con la lista
     * vacía, después en local con la lista puesta pero llegando por otro puerto.
     * <p>
     * De ahí que la configuración se arme por petición: se permite el origen desde el que se
     * pidió —que por definición no es cross-origin— más los que declare la propiedad. Un origen
     * ajeno no listado sigue recibiendo 403, que es lo que se quiere.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            final String origen = request.getHeader(HttpHeaders.ORIGIN);
            if (origen == null) {
                return null;
            }
            final List<String> permitidos = new ArrayList<>(
                    allowedOrigins == null ? List.of() : allowedOrigins);
            final String propio = propio(request);
            if (propio != null && !permitidos.contains(propio)) {
                permitidos.add(propio);
            }
            if (permitidos.isEmpty()) {
                return null;
            }
            final CorsConfiguration configuracion = new CorsConfiguration();
            configuracion.setAllowedOrigins(permitidos);
            configuracion.setAllowedMethods(List.of("*"));
            configuracion.setAllowedHeaders(List.of("*"));
            configuracion.setAllowCredentials(true);
            return configuracion;
        };
    }

    /**
     * El origen de la propia petición, reconstruido del pedido y no del encabezado {@code Origin}
     * —que lo elige el cliente y por lo tanto no prueba nada—.
     */
    private static String propio(jakarta.servlet.http.HttpServletRequest request) {
        final String esquema = request.getScheme();
        final int puerto = request.getServerPort();
        final boolean estandar = ("http".equals(esquema) && puerto == 80)
                || ("https".equals(esquema) && puerto == 443);
        return esquema + "://" + request.getServerName() + (estandar ? "" : ":" + puerto);
    }

    /**
     * Autenticación únicamente por certificado de firma digital.
     * <p>
     * Aquí estuvo {@code .httpBasic(...)}, que junto con la contraseña fija de
     * {@code User.getPassword()} permitía entrar conociendo solo una cédula
     * —dato público— desde internet. La aplicación nunca tuvo contraseñas
     * reales, así que retirar el mecanismo no le quita acceso a nadie.
     */
    /**
     * ROLE_KILLA -- el certificado de servicio de Django/Killa -- sólo entra
     * a /latex/**, /pitia/** y /firma/**. Todo lo demás pide autenticación,
     * igual que hoy, pero le queda vedado a ese certificado en particular.
     * <p>
     * No se usa {@code hasRole("USER")} para "todo lo demás" porque, hoy,
     * {@code user_roles} tiene cero filas en producción para los dos
     * usuarios reales (ver {@link MethodSecurityConfig}): cualquier regla
     * basada en rol para ellos repetiría el mismo incidente que method
     * security ya evitó una vez, dejándolos afuera de su propia aplicación.
     * Esta regla no le exige ningún rol a nadie más que a Killa -- sólo lo
     * excluye a él del resto.
     */
    static AuthorizationManager<RequestAuthorizationContext> autenticadoYNoEsKilla() {
        return (authentication, context) -> {
            final Authentication auth = authentication.get();
            final boolean autenticado = auth != null && auth.isAuthenticated()
                    && !(auth instanceof AnonymousAuthenticationToken);
            if (!autenticado) {
                return new AuthorizationDecision(false);
            }
            final boolean esKilla = auth.getAuthorities().stream()
                    .anyMatch(autoridad -> "ROLE_KILLA".equals(autoridad.getAuthority()));
            return new AuthorizationDecision(!esKilla);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                // Sin esta linea el bean `corsConfigurationSource` existe y nadie lo llama.
                // Estaba asi: la propiedad se documentaba como «para el dev server de Vite» y
                // no emitia un solo encabezado Access-Control-*. Hoy no se nota porque el
                // proxy de Vite hace que todo viaje al mismo origen, pero el dia que alguien
                // llame al API desde otro puerto va a buscar el error en el lugar equivocado.
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/latex/**", "/pitia/**", "/firma/**").hasRole("KILLA")
                        .anyRequest().access(autenticadoYNoEsKilla()))
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

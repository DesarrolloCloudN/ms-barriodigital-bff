package cl.duoc.barriodigital.bff.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import cl.duoc.barriodigital.bff.security.AudienceValidator;
import cl.duoc.barriodigital.bff.security.RolesJwtAuthenticationConverter;

// Esta clase configura toda la seguridad del BFF: como se valida el JWT que
// manda el frontend (firma, expiracion, issuer, audiencia), que rutas son
// publicas y cuales necesitan estar logueado, y las reglas de CORS para que
// el frontend pueda llamar a esta API desde otro origen.
@Configuration
// Habilita el uso de anotaciones como @PreAuthorize en los controllers, que
// es como se revisan los roles (Admin, Funcionario, Vecino) en cada endpoint.
@EnableMethodSecurity
public class SecurityConfig {

    // URL del "issuer" (emisor) del token, es decir, el tenant de Microsoft
    // Entra ID que genera los JWT. Se lee desde application.properties.
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    // Audiencia esperada del token (el identificador de este BFF en Entra
    // ID). Se usa para que el AudienceValidator revise que el token fue
    // pensado para esta aplicacion.
    @Value("${app.security.expected-audience}")
    private String expectedAudience;

    // Origen (dominio) desde el cual se permite que el frontend haga
    // peticiones a esta API (configuracion de CORS).
    @Value("${app.cors.allowed-origin}")
    private String corsAllowedOrigin;

    // Este bean define como se decodifica y valida cada JWT que llega en el
    // header Authorization. Es el corazon de la seguridad del BFF: si el
    // token no pasa estas validaciones, la peticion se rechaza antes de
    // llegar a cualquier controller.
    @Bean
    public JwtDecoder jwtDecoder() {
        // Crea el decoder apuntando al issuer de Entra ID. Este decoder ya
        // sabe descargar las llaves publicas para verificar la firma del
        // token, y valida automaticamente que no este vencido y que el
        // issuer coincida.
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();

        // Validador por defecto: revisa firma, expiracion e issuer.
        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer(issuerUri);
        // Validador propio: revisa que la audiencia (aud) del token sea la
        // de este BFF (ver clase AudienceValidator).
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(expectedAudience);
        // Se combinan ambos validadores para que el token tenga que pasar
        // todas las validaciones (issuer/firma/expiracion Y audiencia).
        OAuth2TokenValidator<Jwt> combinedValidator =
                new DelegatingOAuth2TokenValidator<>(defaultValidators, audienceValidator);

        jwtDecoder.setJwtValidator(combinedValidator);
        return jwtDecoder;
    }

    // Configura CORS: le dice al navegador que esta API acepta peticiones
    // desde el origen del frontend, con estos metodos HTTP y estos headers.
    // Sin esto, el navegador bloquearia las llamadas del frontend por
    // politica de mismo origen.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(corsAllowedOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Permite que se manden cookies/credenciales junto con la peticion.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuracion de CORS a todas las rutas ("/**").
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // Aqui se arma la cadena de filtros de seguridad: que rutas son publicas,
    // cuales requieren estar autenticado, y como se procesa el JWT (usando el
    // converter que agrega los roles) en cada peticion protegida.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RolesJwtAuthenticationConverter rolesJwtAuthenticationConverter) throws Exception {

        http.cors(cors -> {
        }).csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Estas rutas se pueden llamar sin token (login publico
                        // y chequeo de salud del servicio).
                        .requestMatchers("/api/public", "/actuator/health").permitAll()
                        // Todo lo demas necesita venir con un JWT valido.
                        .anyRequest().authenticated())
                // Le dice a Spring Security que esta API es un "resource
                // server" que recibe JWT, y que use nuestro converter para
                // transformar el token en roles utilizables por @PreAuthorize.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(rolesJwtAuthenticationConverter)));

        return http.build();
    }
}

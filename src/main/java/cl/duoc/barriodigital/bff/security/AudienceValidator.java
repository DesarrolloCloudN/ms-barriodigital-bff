package cl.duoc.barriodigital.bff.security;

import java.util.List;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Valida que el claim {@code aud} del JWT contenga la audiencia esperada del
 * BFF (Application ID / Client ID de la App Registration del BFF en Entra
 * ID). Se combina con el validador estándar de issuer/firma/expiración via
 * {@link org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator}.
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    // Audiencia (aud) que se espera encontrar en el token. Basicamente es el
    // identificador de este mismo BFF registrado en Entra ID. Si un token fue
    // generado para otra aplicacion, no deberia servir aqui.
    private final String expectedAudience;

    public AudienceValidator(String expectedAudience) {
        this.expectedAudience = expectedAudience;
    }

    // Este metodo lo invoca Spring Security como parte de la validacion del
    // JWT (ademas de revisar firma, expiracion e issuer, que se hace en
    // SecurityConfig). Aqui solo se revisa que el claim "aud" del token
    // contenga la audiencia esperada.
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audiences = jwt.getAudience();
        if (audiences != null && audiences.contains(expectedAudience)) {
            // La audiencia es la correcta, el token pasa esta validacion.
            return OAuth2TokenValidatorResult.success();
        }
        // Si la audiencia no coincide, se rechaza el token con un error
        // explicando el motivo.
        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "El token no contiene la audiencia esperada del BFF (" + expectedAudience + ")",
                null);
        return OAuth2TokenValidatorResult.failure(error);
    }
}

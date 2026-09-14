package cl.duoc.barriodigital.bff.security;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Convierte el claim "roles" del JWT de Entra ID en GrantedAuthority con prefijo ROLE_. */
@Component
public class RolesJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extraerAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    // Agrega el prefijo ROLE_ a cada rol del claim (ej: "Admin" -> "ROLE_Admin").
    private Collection<GrantedAuthority> extraerAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .map(rol -> (GrantedAuthority) new SimpleGrantedAuthority(ROLE_PREFIX + rol))
                .collect(Collectors.toList());
    }
}

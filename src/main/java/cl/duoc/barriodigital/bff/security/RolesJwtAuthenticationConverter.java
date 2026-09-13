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

/**
 * Convierte el claim {@code roles} del JWT emitido por Microsoft Entra ID
 * (App Roles: Admin, Funcionario, Vecino) en {@link GrantedAuthority} con
 * prefijo {@code ROLE_}, para poder usar {@code hasRole('Admin')} etc. en
 * los controllers via {@code @PreAuthorize}.
 */
@Component
public class RolesJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    // Nombre del claim dentro del JWT donde Entra ID guarda los roles del
    // usuario (por ejemplo: ["Admin"] o ["Vecino"]).
    private static final String ROLES_CLAIM = "roles";
    // Spring Security espera que los roles empiecen con "ROLE_" para que
    // funcionen los @PreAuthorize("hasRole('Admin')") de los controllers.
    private static final String ROLE_PREFIX = "ROLE_";

    // Este metodo lo llama Spring Security automaticamente cada vez que llega
    // un JWT valido. Se encarga de transformar ese JWT en un objeto de
    // autenticacion (JwtAuthenticationToken) que ya trae los roles listos
    // para usar en las validaciones de los controllers.
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extraerAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    // Lee el claim "roles" del token y arma la lista de authorities que
    // necesita Spring Security. Si el token no trae roles, se devuelve una
    // lista vacia (el usuario queda autenticado pero sin ningun rol).
    private Collection<GrantedAuthority> extraerAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        if (roles == null) {
            return List.of();
        }
        // Por cada rol del token (ej: "Admin") se crea un GrantedAuthority
        // con el prefijo "ROLE_" (queda "ROLE_Admin"), que es el formato que
        // entiende hasRole(...) en las anotaciones @PreAuthorize.
        return roles.stream()
                .filter(Objects::nonNull)
                .map(rol -> (GrantedAuthority) new SimpleGrantedAuthority(ROLE_PREFIX + rol))
                .collect(Collectors.toList());
    }
}

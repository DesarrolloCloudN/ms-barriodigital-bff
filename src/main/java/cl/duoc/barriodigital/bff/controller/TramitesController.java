package cl.duoc.barriodigital.bff.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.barriodigital.bff.dto.CambiarEstadoRequest;
import cl.duoc.barriodigital.bff.dto.CrearTramiteInternalRequest;
import cl.duoc.barriodigital.bff.dto.CrearTramiteRequest;
import cl.duoc.barriodigital.bff.dto.Tramite;
import cl.duoc.barriodigital.bff.service.CatalogClientService;
import cl.duoc.barriodigital.bff.service.RequestsClientService;

/**
 * BFF público (detrás del API Gateway) para el módulo de trámites. Ver
 * sección 6 del contrato ("ms-barriodigital-bff").
 */
// Este controller expone los tramites al frontend. Antes de responder, revisa
// que el usuario tenga el rol correcto segun lo que pide la pauta: Admin y
// Funcionario pueden ver/gestionar todos los tramites, mientras que un Vecino
// solo puede ver y crear los suyos propios.
@RestController
@RequestMapping("/api/requests")
public class TramitesController {

    // Nombres de los roles tal como vienen en el claim "roles" del JWT.
    private static final String ROL_ADMIN = "Admin";
    private static final String ROL_FUNCIONARIO = "Funcionario";
    private static final String ROL_VECINO = "Vecino";
    // Nombre del estado que significa que el tramite fue admitido. Cuando un
    // tramite pasa a este estado, hay que descontar cupo del catalogo.
    private static final String ESTADO_ADMITIDO = "ADMITIDO";

    private final RequestsClientService requestsClientService;
    private final CatalogClientService catalogClientService;

    public TramitesController(RequestsClientService requestsClientService,
            CatalogClientService catalogClientService) {
        this.requestsClientService = requestsClientService;
        this.catalogClientService = catalogClientService;
    }

    // Devuelve los tramites: si es Admin o Funcionario ve todos, si es Vecino
    // solo ve los suyos.
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Tramite> listar(@AuthenticationPrincipal Jwt jwt) {
        if (tieneRol(jwt, ROL_ADMIN) || tieneRol(jwt, ROL_FUNCIONARIO)) {
            return requestsClientService.listarTodos();
        }
        return requestsClientService.listarPorVecino(extraerVecinoId(jwt));
    }

    // Devuelve un tramite por id. Si quien pregunta es un Vecino y el
    // tramite no es suyo, se le prohibe verlo (403 Forbidden), aunque el
    // tramite si exista.
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Tramite obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Tramite tramite = requestsClientService.obtenerPorId(id);
        if (esVecino(jwt) && !extraerVecinoId(jwt).equals(tramite.vecinoId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return tramite;
    }

    // Crea un tramite nuevo. La idea es que un Vecino solo pueda crear
    // tramites a su propio nombre (por eso se ignoran los datos del body y se
    // usan los del token), mientras que Admin/Funcionario pueden crear un
    // tramite a nombre de cualquier vecino, indicando sus datos en el body.
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Tramite> crear(@RequestBody CrearTramiteRequest body, @AuthenticationPrincipal Jwt jwt) {
        String vecinoId;
        String vecinoNombre;

        if (esVecino(jwt)) {
            // Si quien crea el tramite es un Vecino, se usan sus propios
            // datos (id y nombre) sacados del token, no lo que venga en el
            // body, para evitar que cree tramites a nombre de otra persona.
            vecinoId = extraerVecinoId(jwt);
            vecinoNombre = extraerNombre(jwt);
        } else {
            // Admin o Funcionario: se confia en los datos del vecino que
            // vienen en el body, porque estan creando el tramite a nombre de
            // otra persona.
            vecinoId = body.vecinoId();
            vecinoNombre = body.vecinoNombre();
        }

        // Se arma el request "interno" con los datos ya resueltos y se manda
        // al microservicio de tramites.
        CrearTramiteInternalRequest internalRequest =
                new CrearTramiteInternalRequest(body.tipoTramiteId(), vecinoId, vecinoNombre, body.descripcion());
        Tramite creado = requestsClientService.crear(internalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // Cambia el estado de un tramite. Solo Admin o Funcionario pueden hacer
    // esto. Lo importante aqui: si el nuevo estado es ADMITIDO, primero hay
    // que descontar 1 cupo del tipo de tramite correspondiente en el
    // catalogo, ANTES de guardar el cambio de estado.
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('Admin') or hasRole('Funcionario')")
    public Tramite cambiarEstado(@PathVariable Long id, @RequestBody CambiarEstadoRequest body) {
        if (ESTADO_ADMITIDO.equalsIgnoreCase(body.estado())) {
            // Se busca el tramite para saber a que tipo de tramite pertenece
            // (y asi saber a cual tipo hay que descontarle el cupo).
            Tramite tramite = requestsClientService.obtenerPorId(id);
            // Si no hay cupo, decrementarCupo lanza ResponseStatusException(409) y
            // NO se llega a llamar a requestsClientService.cambiarEstado(...).
            catalogClientService.decrementarCupo(tramite.tipoTramiteId());
        }
        // Si llegamos hasta aca, o el estado no era ADMITIDO, o si habia
        // cupo y ya se desconto: ahora si se cambia el estado del tramite.
        return requestsClientService.cambiarEstado(id, body);
    }

    // Revisa si el token trae el rol indicado dentro del claim "roles".
    private boolean tieneRol(Jwt jwt, String rol) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains(rol);
    }

    // Un usuario se considera "Vecino puro" solo si tiene el rol Vecino y NO
    // tiene Admin ni Funcionario (por si alguien tuviera varios roles a la
    // vez, se prioriza el rol mas alto).
    private boolean esVecino(Jwt jwt) {
        return tieneRol(jwt, ROL_VECINO) && !tieneRol(jwt, ROL_ADMIN) && !tieneRol(jwt, ROL_FUNCIONARIO);
    }

    // Saca el identificador unico del usuario desde el token. Se usa el
    // claim "oid" (object id de Entra ID) y si no viniera, se usa el
    // "subject" del token como respaldo.
    private String extraerVecinoId(Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        return oid != null ? oid : jwt.getSubject();
    }

    // Saca el nombre del usuario desde el token, usando el claim "name" y si
    // no viene, usando el "preferred_username" como respaldo.
    private String extraerNombre(Jwt jwt) {
        String nombre = jwt.getClaimAsString("name");
        if (nombre != null) {
            return nombre;
        }
        return jwt.getClaimAsString("preferred_username");
    }
}

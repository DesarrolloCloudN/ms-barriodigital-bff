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

/** BFF publico del modulo de tramites (ver contrato, seccion 6). */
@RestController
@RequestMapping("/api/requests")
public class TramitesController {

    private static final String ROL_ADMIN = "Admin";
    private static final String ROL_VECINO = "Vecino";
    // Al pasar a este estado hay que descontar cupo del tipo de tramite.
    private static final String ESTADO_ADMITIDO = "ADMITIDO";

    private final RequestsClientService requestsClientService;
    private final CatalogClientService catalogClientService;

    public TramitesController(RequestsClientService requestsClientService,
            CatalogClientService catalogClientService) {
        this.requestsClientService = requestsClientService;
        this.catalogClientService = catalogClientService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Tramite> listar(@AuthenticationPrincipal Jwt jwt) {
        if (tieneRol(jwt, ROL_ADMIN)) {
            return requestsClientService.listarTodos();
        }
        return requestsClientService.listarPorVecino(extraerVecinoId(jwt));
    }

    // Si un Vecino pide un tramite ajeno, se responde 403 aunque exista.
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Tramite obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Tramite tramite = requestsClientService.obtenerPorId(id);
        if (esVecino(jwt) && !extraerVecinoId(jwt).equals(tramite.vecinoId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return tramite;
    }

    // Si es Vecino se usan sus datos del token, ignorando el body (evita suplantar a otro).
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Tramite> crear(@RequestBody CrearTramiteRequest body, @AuthenticationPrincipal Jwt jwt) {
        String vecinoId;
        String vecinoNombre;

        if (esVecino(jwt)) {
            vecinoId = extraerVecinoId(jwt);
            vecinoNombre = extraerNombre(jwt);
        } else {
            vecinoId = body.vecinoId();
            vecinoNombre = body.vecinoNombre();
        }

        CrearTramiteInternalRequest internalRequest =
                new CrearTramiteInternalRequest(body.tipoTramiteId(), vecinoId, vecinoNombre, body.descripcion());
        Tramite creado = requestsClientService.crear(internalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // Si pasa a ADMITIDO, se descuenta cupo del catalogo antes de guardar el estado.
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('Admin')")
    public Tramite cambiarEstado(@PathVariable Long id, @RequestBody CambiarEstadoRequest body) {
        if (ESTADO_ADMITIDO.equalsIgnoreCase(body.estado())) {
            Tramite tramite = requestsClientService.obtenerPorId(id);
            catalogClientService.decrementarCupo(tramite.tipoTramiteId());
        }
        return requestsClientService.cambiarEstado(id, body);
    }

    private boolean tieneRol(Jwt jwt, String rol) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains(rol);
    }

    // Se considera Vecino puro solo si no tiene ademas el rol Admin.
    private boolean esVecino(Jwt jwt) {
        return tieneRol(jwt, ROL_VECINO) && !tieneRol(jwt, ROL_ADMIN);
    }

    private String extraerVecinoId(Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        return oid != null ? oid : jwt.getSubject();
    }

    private String extraerNombre(Jwt jwt) {
        String nombre = jwt.getClaimAsString("name");
        if (nombre != null) {
            return nombre;
        }
        return jwt.getClaimAsString("preferred_username");
    }
}

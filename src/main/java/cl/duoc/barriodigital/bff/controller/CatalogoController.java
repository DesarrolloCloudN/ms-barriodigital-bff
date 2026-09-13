package cl.duoc.barriodigital.bff.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.barriodigital.bff.dto.ActualizarTipoTramiteRequest;
import cl.duoc.barriodigital.bff.dto.CrearTipoTramiteRequest;
import cl.duoc.barriodigital.bff.dto.TipoTramite;
import cl.duoc.barriodigital.bff.service.CatalogClientService;

/**
 * BFF público (detrás del API Gateway) para el catálogo de tipos de trámite.
 * Ver sección 6 del contrato ("ms-barriodigital-bff").
 */
// Este controller expone el catalogo de tipos de tramite al frontend. Cada
// endpoint tiene su propia regla de rol: ver el catalogo lo puede hacer
// cualquiera logueado, pero crear/editar/eliminar tipos de tramite esta mas
// restringido.
@RestController
@RequestMapping("/api/catalog")
public class CatalogoController {

    private final CatalogClientService catalogClientService;

    public CatalogoController(CatalogClientService catalogClientService) {
        this.catalogClientService = catalogClientService;
    }

    // Lista todos los tipos de tramite. Cualquier usuario autenticado puede
    // verlos (Admin, Funcionario o Vecino).
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TipoTramite> listar() {
        return catalogClientService.listar();
    }

    // Crea un tipo de tramite nuevo. Solo el Admin puede hacerlo.
    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<TipoTramite> crear(@RequestBody CrearTipoTramiteRequest body) {
        TipoTramite creado = catalogClientService.crear(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // Actualiza un tipo de tramite existente. Lo pueden hacer Admin o
    // Funcionario.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin') or hasRole('Funcionario')")
    public TipoTramite actualizar(@PathVariable Long id, @RequestBody ActualizarTipoTramiteRequest body) {
        return catalogClientService.actualizar(id, body);
    }

    // Elimina un tipo de tramite. Solo el Admin puede hacerlo. Responde 204
    // (sin contenido) si la eliminacion fue exitosa.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        catalogClientService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

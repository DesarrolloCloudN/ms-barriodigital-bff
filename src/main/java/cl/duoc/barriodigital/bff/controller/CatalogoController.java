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

/** BFF publico del catalogo de tipos de tramite (ver contrato, seccion 6). */
@RestController
@RequestMapping("/api/catalog")
public class CatalogoController {

    private final CatalogClientService catalogClientService;

    public CatalogoController(CatalogClientService catalogClientService) {
        this.catalogClientService = catalogClientService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TipoTramite> listar() {
        return catalogClientService.listar();
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<TipoTramite> crear(@RequestBody CrearTipoTramiteRequest body) {
        TipoTramite creado = catalogClientService.crear(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public TipoTramite actualizar(@PathVariable Long id, @RequestBody ActualizarTipoTramiteRequest body) {
        return catalogClientService.actualizar(id, body);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        catalogClientService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

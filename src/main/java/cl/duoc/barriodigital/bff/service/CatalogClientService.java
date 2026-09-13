package cl.duoc.barriodigital.bff.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.barriodigital.bff.dto.ActualizarTipoTramiteRequest;
import cl.duoc.barriodigital.bff.dto.CrearTipoTramiteRequest;
import cl.duoc.barriodigital.bff.dto.TipoTramite;

/**
 * Encapsula las llamadas HTTP del BFF hacia ms-barriodigital-catalog
 * (puerto 8082, interno, sin seguridad propia). Ver sección 6 del contrato.
 */
@Service
public class CatalogClientService {

    // Cliente HTTP configurado con la URL base del microservicio de
    // catalogo, para no tener que repetirla en cada metodo.
    private final RestClient restClient;

    public CatalogClientService(@Value("${app.services.catalog-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    // Trae todos los tipos de tramite del catalogo.
    public List<TipoTramite> listar() {
        return restClient.get()
                .uri("/api/catalog")
                .retrieve()
                .body(new ParameterizedTypeReference<List<TipoTramite>>() {
                });
    }

    // Busca un tipo de tramite por id. Si no existe, se traduce el 404 del
    // microservicio a una excepcion que el BFF tambien responde como 404.
    public TipoTramite obtenerPorId(Long id) {
        try {
            return restClient.get()
                    .uri("/api/catalog/{id}", id)
                    .retrieve()
                    .body(TipoTramite.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de trámite no encontrado", e);
        }
    }

    // Crea un tipo de tramite nuevo en el catalogo.
    public TipoTramite crear(CrearTipoTramiteRequest body) {
        return restClient.post()
                .uri("/api/catalog")
                .body(body)
                .retrieve()
                .body(TipoTramite.class);
    }

    // Actualiza los datos de un tipo de tramite existente.
    public TipoTramite actualizar(Long id, ActualizarTipoTramiteRequest body) {
        try {
            return restClient.put()
                    .uri("/api/catalog/{id}", id)
                    .body(body)
                    .retrieve()
                    .body(TipoTramite.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de trámite no encontrado", e);
        }
    }

    /**
     * Decrementa en 1 el cupo disponible hoy del tipo de trámite indicado.
     * Se invoca desde el BFF antes de admitir un trámite (sección 5). Si el
     * microservicio de catálogo responde 409 (sin cupo), se propaga como
     * {@link ResponseStatusException} 409 para que el controller NO llame al
     * microservicio de requests.
     */
    // En palabras simples: esto es lo que hace que un tramite no se pueda
    // admitir si ya no queda cupo para ese tipo de tramite hoy. Se llama
    // ANTES de cambiar el estado del tramite a ADMITIDO (ver
    // TramitesController.cambiarEstado). Si no hay cupo, se lanza una
    // excepcion 409 y el flujo se corta ahi, sin llegar a admitir el tramite.
    public void decrementarCupo(Long id) {
        try {
            restClient.patch()
                    .uri("/api/catalog/{id}/decrementar-cupo", id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No hay cupo disponible para este tipo de trámite", e);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de trámite no encontrado", e);
        }
    }

    // Devuelve 1 unidad de cupo al tipo de tramite indicado. Es como la
    // operacion contraria a decrementarCupo, se usaria por ejemplo si un
    // tramite admitido se termina rechazando despues.
    public void reponerCupo(Long id) {
        try {
            restClient.patch()
                    .uri("/api/catalog/{id}/reponer-cupo", id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de trámite no encontrado", e);
        }
    }

    // Elimina un tipo de tramite del catalogo.
    public void eliminar(Long id) {
        try {
            restClient.delete()
                    .uri("/api/catalog/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de trámite no encontrado", e);
        }
    }
}

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

/** Encapsula las llamadas HTTP del BFF hacia ms-barriodigital-catalog (puerto 8082). */
@Service
public class CatalogClientService {

    private final RestClient restClient;

    public CatalogClientService(@Value("${app.services.catalog-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<TipoTramite> listar() {
        return restClient.get()
                .uri("/api/catalog")
                .retrieve()
                .body(new ParameterizedTypeReference<List<TipoTramite>>() {
                });
    }

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

    public TipoTramite crear(CrearTipoTramiteRequest body) {
        return restClient.post()
                .uri("/api/catalog")
                .body(body)
                .retrieve()
                .body(TipoTramite.class);
    }

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

    // Descuenta un cupo; sin cupo disponible lanza 409 y no se admite el tramite.
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

    // Devuelve el cupo descontado; se usa si un tramite admitido se rechaza despues.
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

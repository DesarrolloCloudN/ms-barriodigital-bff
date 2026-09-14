package cl.duoc.barriodigital.bff.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.barriodigital.bff.dto.CambiarEstadoRequest;
import cl.duoc.barriodigital.bff.dto.CrearTramiteInternalRequest;
import cl.duoc.barriodigital.bff.dto.Tramite;

/** Encapsula las llamadas HTTP del BFF hacia ms-barriodigital-requests (puerto 8081). */
@Service
public class RequestsClientService {

    private final RestClient restClient;

    public RequestsClientService(@Value("${app.services.requests-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<Tramite> listarTodos() {
        return restClient.get()
                .uri("/api/requests")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Tramite>>() {
                });
    }

    public List<Tramite> listarPorVecino(String vecinoId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/requests").queryParam("vecinoId", vecinoId).build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Tramite>>() {
                });
    }

    public Tramite obtenerPorId(Long id) {
        try {
            return restClient.get()
                    .uri("/api/requests/{id}", id)
                    .retrieve()
                    .body(Tramite.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trámite no encontrado", e);
        }
    }

    public Tramite crear(CrearTramiteInternalRequest body) {
        return restClient.post()
                .uri("/api/requests")
                .body(body)
                .retrieve()
                .body(Tramite.class);
    }

    // Traduce 404 (no existe) y 409 (transicion de estado invalida) del microservicio.
    public Tramite cambiarEstado(Long id, CambiarEstadoRequest body) {
        try {
            return restClient.put()
                    .uri("/api/requests/{id}/estado", id)
                    .body(body)
                    .retrieve()
                    .body(Tramite.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trámite no encontrado", e);
        } catch (HttpClientErrorException.Conflict e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transición de estado inválida", e);
        }
    }
}

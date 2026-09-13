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

/**
 * Encapsula las llamadas HTTP del BFF hacia ms-barriodigital-requests
 * (puerto 8081, interno, sin seguridad propia). Ver sección 6 del contrato.
 */
@Service
public class RequestsClientService {

    // Cliente HTTP que usamos para llamar al microservicio de tramites. Ya
    // viene configurado con la URL base, asi que en cada metodo solo hay que
    // indicar el path especifico.
    private final RestClient restClient;

    public RequestsClientService(@Value("${app.services.requests-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    // Trae todos los tramites, sin filtrar por vecino. Lo usan Admin y
    // Funcionario (ver TramitesController).
    public List<Tramite> listarTodos() {
        return restClient.get()
                .uri("/api/requests")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Tramite>>() {
                });
    }

    // Trae solo los tramites de un vecino en particular, usando un query
    // param. Lo usan los vecinos para ver unicamente sus propios tramites.
    public List<Tramite> listarPorVecino(String vecinoId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/requests").queryParam("vecinoId", vecinoId).build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Tramite>>() {
                });
    }

    // Busca un tramite por su id. Si el microservicio responde 404, se
    // traduce a una excepcion que hace que el BFF tambien responda 404.
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

    // Crea un tramite nuevo, mandando el body ya armado (con vecinoId y
    // vecinoNombre definitivos) al microservicio de tramites.
    public Tramite crear(CrearTramiteInternalRequest body) {
        return restClient.post()
                .uri("/api/requests")
                .body(body)
                .retrieve()
                .body(Tramite.class);
    }

    // Cambia el estado de un tramite (por ejemplo a ADMITIDO o RECHAZADO). Si
    // el tramite no existe, se responde 404; si el cambio de estado no es
    // valido (por ejemplo pasar de un estado final a otro), se responde 409.
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

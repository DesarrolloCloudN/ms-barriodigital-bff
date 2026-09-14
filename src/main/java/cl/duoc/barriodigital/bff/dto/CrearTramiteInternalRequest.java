package cl.duoc.barriodigital.bff.dto;

// Body interno hacia el microservicio, ya con vecinoId/vecinoNombre resueltos.
public record CrearTramiteInternalRequest(
        Long tipoTramiteId,
        String vecinoId,
        String vecinoNombre,
        String descripcion) {
}

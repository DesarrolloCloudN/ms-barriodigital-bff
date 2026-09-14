package cl.duoc.barriodigital.bff.dto;

// Body de POST /api/requests; si es Vecino se ignoran vecinoId/vecinoNombre del body.
public record CrearTramiteRequest(
        Long tipoTramiteId,
        String descripcion,
        String vecinoId,
        String vecinoNombre) {
}

package cl.duoc.barriodigital.bff.dto;

// Body de POST /api/requests; vecinoId/vecinoNombre se ignoran (siempre se usan los del JWT).
public record CrearTramiteRequest(
        Long tipoTramiteId,
        String descripcion,
        String vecinoId,
        String vecinoNombre) {
}

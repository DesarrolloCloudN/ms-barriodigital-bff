package cl.duoc.barriodigital.bff.dto;

// Body de POST /api/catalog; solo el rol Admin puede usarlo.
public record CrearTipoTramiteRequest(
        String nombre,
        String descripcion,
        String requisitos,
        Integer cupoDiario) {
}

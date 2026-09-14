package cl.duoc.barriodigital.bff.dto;

// Body de PUT /api/catalog/{id}; solo lo usa Admin.
public record ActualizarTipoTramiteRequest(
        String nombre,
        String descripcion,
        String requisitos,
        Integer cupoDiario,
        Boolean activo) {
}

package cl.duoc.barriodigital.bff.dto;

// Body de PUT /api/catalog/{id}; lo usan Admin y Funcionario.
public record ActualizarTipoTramiteRequest(
        String nombre,
        String descripcion,
        String requisitos,
        Integer cupoDiario,
        Boolean activo) {
}

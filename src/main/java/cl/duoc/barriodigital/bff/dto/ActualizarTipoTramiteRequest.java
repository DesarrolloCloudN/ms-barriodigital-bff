package cl.duoc.barriodigital.bff.dto;

/**
 * Body de {@code PUT /api/catalog/{id}} (sección 6).
 */
// Datos para actualizar un tipo de tramite existente (incluye si esta activo
// o no). Lo pueden usar Admin y Funcionario.
public record ActualizarTipoTramiteRequest(
        String nombre,
        String descripcion,
        String requisitos,
        Integer cupoDiario,
        Boolean activo) {
}

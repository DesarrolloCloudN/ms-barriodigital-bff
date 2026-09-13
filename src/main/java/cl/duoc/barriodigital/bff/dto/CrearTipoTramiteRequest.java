package cl.duoc.barriodigital.bff.dto;

/**
 * Body de {@code POST /api/catalog} (sección 6).
 */
// Datos para crear un nuevo tipo de tramite en el catalogo. Solo el rol
// Admin puede crear tipos de tramite nuevos.
public record CrearTipoTramiteRequest(
        String nombre,
        String descripcion,
        String requisitos,
        Integer cupoDiario) {
}

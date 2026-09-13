package cl.duoc.barriodigital.bff.dto;

/**
 * Representa la entidad {@code TipoTramite} (tabla {@code TIPOS_TRAMITE})
 * expuesta por ms-barriodigital-catalog. Ver sección 5 del contrato.
 */
// Representa un tipo de tramite del catalogo (por ejemplo "Certificado de
// residencia"), con su cupo diario y cuanto cupo queda disponible hoy.
public record TipoTramite(
        Long id,
        String nombre,
        String descripcion,
        String requisitos,
        Integer cupoDiario,
        Integer cupoDisponibleHoy,
        Boolean activo) {
}

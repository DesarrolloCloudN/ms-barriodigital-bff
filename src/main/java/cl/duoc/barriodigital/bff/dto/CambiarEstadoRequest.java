package cl.duoc.barriodigital.bff.dto;

/**
 * Body de {@code PUT /api/requests/{id}/estado} (sección 6).
 */
// Datos que se mandan para cambiar el estado de un tramite (por ejemplo
// pasarlo a "ADMITIDO", "RECHAZADO", etc). Solo Admin o Funcionario pueden
// hacer esto (ver TramitesController).
public record CambiarEstadoRequest(
        String estado,
        String funcionarioAsignado,
        String observaciones) {
}

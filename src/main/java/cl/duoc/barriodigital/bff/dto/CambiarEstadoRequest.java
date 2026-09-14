package cl.duoc.barriodigital.bff.dto;

// Body de PUT /api/requests/{id}/estado; solo Admin o Funcionario.
public record CambiarEstadoRequest(
        String estado,
        String funcionarioAsignado,
        String observaciones) {
}

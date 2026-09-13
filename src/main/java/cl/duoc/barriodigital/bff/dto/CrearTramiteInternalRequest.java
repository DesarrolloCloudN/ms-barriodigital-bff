package cl.duoc.barriodigital.bff.dto;

/**
 * Body que el BFF envía a {@code POST /api/requests} de
 * ms-barriodigital-requests (sección 6), ya con {@code vecinoId}/
 * {@code vecinoNombre} resueltos.
 */
// Este es el body "interno" que el BFF arma para reenviarselo al
// microservicio de tramites, ya con el vecinoId/vecinoNombre definitivos
// (resueltos en TramitesController, ya sea desde el token o desde el body
// original si quien crea es Admin/Funcionario).
public record CrearTramiteInternalRequest(
        Long tipoTramiteId,
        String vecinoId,
        String vecinoNombre,
        String descripcion) {
}

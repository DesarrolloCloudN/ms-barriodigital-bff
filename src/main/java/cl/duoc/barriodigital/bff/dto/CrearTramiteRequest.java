package cl.duoc.barriodigital.bff.dto;

/**
 * Body de entrada de {@code POST /api/requests} en el BFF (sección 6).
 * {@code vecinoId}/{@code vecinoNombre} son ignorados por el BFF si quien
 * llama tiene rol Vecino (se reemplazan por los claims del propio token);
 * son obligatorios si quien llama es Admin/Funcionario (creando a nombre de
 * un vecino).
 */
// Esto es lo que el frontend manda cuando se crea un tramite nuevo. Ojo: si
// quien crea el tramite es un Vecino, el controller ignora los campos
// vecinoId/vecinoNombre que vengan aqui y usa los datos del propio token.
public record CrearTramiteRequest(
        Long tipoTramiteId,
        String descripcion,
        String vecinoId,
        String vecinoNombre) {
}

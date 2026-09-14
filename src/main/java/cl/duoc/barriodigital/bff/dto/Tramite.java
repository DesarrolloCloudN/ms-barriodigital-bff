package cl.duoc.barriodigital.bff.dto;

import java.time.LocalDateTime;

public record Tramite(
        Long id,
        Long tipoTramiteId,
        String vecinoId,
        String vecinoNombre,
        String descripcion,
        String estado,
        String funcionarioAsignado,
        String observaciones,
        LocalDateTime fechaIngreso,
        LocalDateTime fechaActualizacion) {
}

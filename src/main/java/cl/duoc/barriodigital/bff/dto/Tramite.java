package cl.duoc.barriodigital.bff.dto;

import java.time.LocalDateTime;

public record Tramite(
        Long id,
        Long tipoTramiteId,
        String vecinoId,
        String vecinoNombre,
        String descripcion,
        String estado,
        String responsableAsignado,
        String observaciones,
        LocalDateTime fechaIngreso,
        LocalDateTime fechaActualizacion) {
}

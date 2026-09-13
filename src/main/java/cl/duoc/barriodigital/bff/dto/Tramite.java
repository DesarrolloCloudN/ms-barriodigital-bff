package cl.duoc.barriodigital.bff.dto;

import java.time.LocalDateTime;

/**
 * Representa la entidad {@code Tramite} (tabla {@code TRAMITES}) expuesta por
 * ms-barriodigital-requests. Ver sección 5 del contrato.
 */
// Un record en Java es una forma corta de crear una clase que solo guarda
// datos (con constructor, getters y equals/hashCode generados automaticos).
// Este representa un tramite tal cual lo devuelve el microservicio de
// trámites.
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

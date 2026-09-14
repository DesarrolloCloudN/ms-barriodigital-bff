package cl.duoc.barriodigital.bff.dto;

public record TipoTramite(
        Long id,
        String nombre,
        String descripcion,
        String requisitos,
        Integer cupoDiario,
        Integer cupoDisponibleHoy,
        Boolean activo) {
}

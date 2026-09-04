package com.aitsolutions.crm.auditoria;

import java.time.LocalDateTime;

public record AuditoriaEventoResponse(
        Long id, LocalDateTime fecha, String usuario, String metodo,
        String ruta, int estadoHttp, String direccionIp) {
    public AuditoriaEventoResponse(AuditoriaEvento evento) {
        this(evento.getId(), evento.getFecha(), evento.getUsuario(), evento.getMetodo(),
                evento.getRuta(), evento.getEstadoHttp(), evento.getDireccionIp());
    }
}

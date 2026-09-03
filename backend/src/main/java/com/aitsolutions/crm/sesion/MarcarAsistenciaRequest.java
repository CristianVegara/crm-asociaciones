package com.aitsolutions.crm.sesion;

import jakarta.validation.constraints.NotNull;

public class MarcarAsistenciaRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoSesion estado;

    public EstadoSesion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSesion estado) {
        this.estado = estado;
    }
}

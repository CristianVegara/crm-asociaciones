package com.aitsolutions.crm.planservicio;

import jakarta.validation.constraints.NotNull;

public class CambiarEstadoPlanRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoPlanServicio estado;

    public EstadoPlanServicio getEstado() {
        return estado;
    }

    public void setEstado(EstadoPlanServicio estado) {
        this.estado = estado;
    }
}

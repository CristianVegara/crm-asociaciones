package com.aitsolutions.crm.tiposervicio;

import jakarta.validation.constraints.NotNull;

public class ResponsableItem {

    @NotNull(message = "El rol es obligatorio")
    private Long rolId;

    @NotNull(message = "La capacidad es obligatoria")
    private CapacidadServicio capacidad;

    public Long getRolId() {
        return rolId;
    }

    public void setRolId(Long rolId) {
        this.rolId = rolId;
    }

    public CapacidadServicio getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(CapacidadServicio capacidad) {
        this.capacidad = capacidad;
    }
}

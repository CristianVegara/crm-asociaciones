package com.aitsolutions.crmclient.dto;

public class ResponsableItem {

    private Long rolId;
    private CapacidadServicio capacidad;

    public ResponsableItem(Long rolId, CapacidadServicio capacidad) {
        this.rolId = rolId;
        this.capacidad = capacidad;
    }

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

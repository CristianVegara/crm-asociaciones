package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponsableResponse {

    private Long rolId;
    private String rolNombre;
    private CapacidadServicio capacidad;

    public Long getRolId() {
        return rolId;
    }

    public void setRolId(Long rolId) {
        this.rolId = rolId;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public CapacidadServicio getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(CapacidadServicio capacidad) {
        this.capacidad = capacidad;
    }
}

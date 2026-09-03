package com.aitsolutions.crmclient.dto;

public class TipoServicioRequest {

    private String nombre;

    public TipoServicioRequest(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

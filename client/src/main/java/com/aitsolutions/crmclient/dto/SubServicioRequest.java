package com.aitsolutions.crmclient.dto;

public class SubServicioRequest {
    private String nombre;

    public SubServicioRequest(String nombre) { this.nombre = nombre; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}

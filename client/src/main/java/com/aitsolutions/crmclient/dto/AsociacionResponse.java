package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AsociacionResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private String contacto;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getContacto() { return contacto; }
    @Override public String toString() { return nombre; }
}

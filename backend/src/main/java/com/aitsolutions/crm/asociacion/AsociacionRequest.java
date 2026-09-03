package com.aitsolutions.crm.asociacion;

import jakarta.validation.constraints.NotBlank;

public class AsociacionRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String direccion;

    private String contacto;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }
}

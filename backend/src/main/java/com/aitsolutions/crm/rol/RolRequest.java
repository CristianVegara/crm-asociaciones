package com.aitsolutions.crm.rol;

import jakarta.validation.constraints.NotBlank;

public class RolRequest {

    @NotBlank(message = "El nombre del rol es obligatorio")
    private String nombre;

    private String descripcion;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

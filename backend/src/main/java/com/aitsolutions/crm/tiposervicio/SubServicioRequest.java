package com.aitsolutions.crm.tiposervicio;

import jakarta.validation.constraints.NotBlank;

public class SubServicioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

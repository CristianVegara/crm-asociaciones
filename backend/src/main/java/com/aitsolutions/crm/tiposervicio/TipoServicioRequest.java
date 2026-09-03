package com.aitsolutions.crm.tiposervicio;

import jakarta.validation.constraints.NotBlank;

public class TipoServicioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String icono;

    private String color;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

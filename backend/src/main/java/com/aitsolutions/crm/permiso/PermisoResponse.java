package com.aitsolutions.crm.permiso;

public class PermisoResponse {

    private final String nombre;
    private final String descripcion;

    public PermisoResponse(Permiso permiso) {
        this.nombre = permiso.name();
        this.descripcion = permiso.getDescripcion();
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

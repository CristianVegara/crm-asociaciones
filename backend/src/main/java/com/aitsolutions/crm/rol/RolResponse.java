package com.aitsolutions.crm.rol;

import com.aitsolutions.crm.permiso.Permiso;

import java.util.Set;

public class RolResponse {

    private final Long id;
    private final String nombre;
    private final String descripcion;
    private final Set<Permiso> permisos;

    public RolResponse(Rol rol) {
        this.id = rol.getId();
        this.nombre = rol.getNombre();
        this.descripcion = rol.getDescripcion();
        this.permisos = rol.getPermisos();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Set<Permiso> getPermisos() {
        return permisos;
    }
}

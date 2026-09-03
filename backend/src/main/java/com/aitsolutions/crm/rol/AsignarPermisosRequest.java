package com.aitsolutions.crm.rol;

import com.aitsolutions.crm.permiso.Permiso;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public class AsignarPermisosRequest {

    @NotNull
    private Set<Permiso> permisos;

    public Set<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<Permiso> permisos) {
        this.permisos = permisos;
    }
}

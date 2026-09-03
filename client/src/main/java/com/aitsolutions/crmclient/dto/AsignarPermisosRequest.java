package com.aitsolutions.crmclient.dto;

import java.util.Set;

public class AsignarPermisosRequest {

    private Set<String> permisos;

    public AsignarPermisosRequest(Set<String> permisos) {
        this.permisos = permisos;
    }

    public Set<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<String> permisos) {
        this.permisos = permisos;
    }
}

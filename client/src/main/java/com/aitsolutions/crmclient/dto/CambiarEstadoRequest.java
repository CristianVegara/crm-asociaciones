package com.aitsolutions.crmclient.dto;

public class CambiarEstadoRequest {

    private boolean activo;

    public CambiarEstadoRequest(boolean activo) {
        this.activo = activo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}

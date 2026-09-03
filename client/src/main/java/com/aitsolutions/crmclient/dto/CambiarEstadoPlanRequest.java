package com.aitsolutions.crmclient.dto;

public class CambiarEstadoPlanRequest {
    private String estado;

    public CambiarEstadoPlanRequest(String estado) { this.estado = estado; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

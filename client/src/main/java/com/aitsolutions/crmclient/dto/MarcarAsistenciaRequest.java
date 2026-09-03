package com.aitsolutions.crmclient.dto;

public class MarcarAsistenciaRequest {
    private String estado;

    public MarcarAsistenciaRequest(String estado) { this.estado = estado; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

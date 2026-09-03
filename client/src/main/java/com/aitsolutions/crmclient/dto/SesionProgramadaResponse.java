package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SesionProgramadaResponse {
    private Long id;
    private Long planServicioId;
    private String fechaPrevista;
    private String estado;
    private Long registradoPorId;
    private String fechaRegistro;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPlanServicioId() { return planServicioId; }
    public void setPlanServicioId(Long planServicioId) { this.planServicioId = planServicioId; }
    public String getFechaPrevista() { return fechaPrevista; }
    public void setFechaPrevista(String fechaPrevista) { this.fechaPrevista = fechaPrevista; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Long getRegistradoPorId() { return registradoPorId; }
    public void setRegistradoPorId(Long registradoPorId) { this.registradoPorId = registradoPorId; }
    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}

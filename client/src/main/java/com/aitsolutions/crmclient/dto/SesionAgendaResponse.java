package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SesionAgendaResponse {
    private Long id;
    private Long planServicioId;
    private Long pacienteId;
    private String pacienteNombreCompleto;
    private String tipoServicioNombre;
    private String subServicioNombre;
    private String fechaPrevista;
    private String estado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPlanServicioId() { return planServicioId; }
    public void setPlanServicioId(Long planServicioId) { this.planServicioId = planServicioId; }
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    public String getPacienteNombreCompleto() { return pacienteNombreCompleto; }
    public void setPacienteNombreCompleto(String value) { this.pacienteNombreCompleto = value; }
    public String getTipoServicioNombre() { return tipoServicioNombre; }
    public void setTipoServicioNombre(String value) { this.tipoServicioNombre = value; }
    public String getSubServicioNombre() { return subServicioNombre; }
    public void setSubServicioNombre(String value) { this.subServicioNombre = value; }
    public String getFechaPrevista() { return fechaPrevista; }
    public void setFechaPrevista(String value) { this.fechaPrevista = value; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

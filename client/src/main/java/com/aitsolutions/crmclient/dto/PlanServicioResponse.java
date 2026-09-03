package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanServicioResponse {
    private Long id;
    private Long pacienteId;
    private String pacienteNombreCompleto;
    private Long tipoServicioId;
    private String tipoServicioNombre;
    private Long subServicioId;
    private String subServicioNombre;
    private Set<String> diasSemana;
    private String fechaInicio;
    private String fechaFin;
    private String estado;
    private List<SesionProgramadaResponse> sesiones = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    public String getPacienteNombreCompleto() { return pacienteNombreCompleto; }
    public void setPacienteNombreCompleto(String value) { pacienteNombreCompleto = value; }
    public Long getTipoServicioId() { return tipoServicioId; }
    public void setTipoServicioId(Long value) { tipoServicioId = value; }
    public String getTipoServicioNombre() { return tipoServicioNombre; }
    public void setTipoServicioNombre(String value) { tipoServicioNombre = value; }
    public Long getSubServicioId() { return subServicioId; }
    public void setSubServicioId(Long value) { subServicioId = value; }
    public String getSubServicioNombre() { return subServicioNombre; }
    public void setSubServicioNombre(String value) { subServicioNombre = value; }
    public Set<String> getDiasSemana() { return diasSemana; }
    public void setDiasSemana(Set<String> value) { diasSemana = value; }
    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String value) { fechaInicio = value; }
    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String value) { fechaFin = value; }
    public String getEstado() { return estado; }
    public void setEstado(String value) { estado = value; }
    public List<SesionProgramadaResponse> getSesiones() { return sesiones; }
    public void setSesiones(List<SesionProgramadaResponse> value) { sesiones = value; }
}

package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SancionResponse {

    private Long id;
    private Long pacienteId;
    private String pacienteNombreCompleto;
    private Long planServicioId;
    private TipoSancion tipo;
    private String fecha;
    private String motivo;
    private String aplicadaPorNombre;
    private boolean automatica;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getPacienteNombreCompleto() {
        return pacienteNombreCompleto;
    }

    public void setPacienteNombreCompleto(String pacienteNombreCompleto) {
        this.pacienteNombreCompleto = pacienteNombreCompleto;
    }

    public Long getPlanServicioId() {
        return planServicioId;
    }

    public void setPlanServicioId(Long planServicioId) {
        this.planServicioId = planServicioId;
    }

    public TipoSancion getTipo() {
        return tipo;
    }

    public void setTipo(TipoSancion tipo) {
        this.tipo = tipo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getAplicadaPorNombre() {
        return aplicadaPorNombre;
    }

    public void setAplicadaPorNombre(String aplicadaPorNombre) {
        this.aplicadaPorNombre = aplicadaPorNombre;
    }

    public boolean isAutomatica() {
        return automatica;
    }

    public void setAutomatica(boolean automatica) {
        this.automatica = automatica;
    }
}

package com.aitsolutions.crmclient.dto;

public class SancionRequest {

    private Long pacienteId;
    private Long planServicioId;
    private TipoSancion tipo;
    private String motivo;

    public SancionRequest(Long pacienteId, Long planServicioId, TipoSancion tipo, String motivo) {
        this.pacienteId = pacienteId;
        this.planServicioId = planServicioId;
        this.tipo = tipo;
        this.motivo = motivo;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}

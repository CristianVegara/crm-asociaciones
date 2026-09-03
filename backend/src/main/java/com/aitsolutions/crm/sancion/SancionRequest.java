package com.aitsolutions.crm.sancion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SancionRequest {

    @NotNull(message = "El paciente es obligatorio")
    private Long pacienteId;

    // Opcional: una sancion puede no estar ligada a un plan concreto (p.ej. tarjeta por agresión).
    private Long planServicioId;

    @NotNull(message = "El tipo de sanción es obligatorio")
    private TipoSancion tipo;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

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

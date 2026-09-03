package com.aitsolutions.crm.sancion;

import java.time.LocalDate;

public class SancionResponse {

    private final Long id;
    private final Long pacienteId;
    private final String pacienteNombreCompleto;
    private final Long planServicioId;
    private final TipoSancion tipo;
    private final LocalDate fecha;
    private final String motivo;
    private final Long aplicadaPorId;
    private final String aplicadaPorNombre;
    private final boolean automatica;

    public SancionResponse(Sancion sancion) {
        this.id = sancion.getId();
        this.pacienteId = sancion.getPaciente().getId();
        this.pacienteNombreCompleto = sancion.getPaciente().getNombre() + " " + sancion.getPaciente().getApellidos();
        this.planServicioId = sancion.getPlanServicio() != null ? sancion.getPlanServicio().getId() : null;
        this.tipo = sancion.getTipo();
        this.fecha = sancion.getFecha();
        this.motivo = sancion.getMotivo();
        this.aplicadaPorId = sancion.getAplicadaPor().getId();
        this.aplicadaPorNombre = sancion.getAplicadaPor().getNombre() + " " + sancion.getAplicadaPor().getApellidos();
        this.automatica = sancion.isAutomatica();
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public String getPacienteNombreCompleto() {
        return pacienteNombreCompleto;
    }

    public Long getPlanServicioId() {
        return planServicioId;
    }

    public TipoSancion getTipo() {
        return tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public Long getAplicadaPorId() {
        return aplicadaPorId;
    }

    public String getAplicadaPorNombre() {
        return aplicadaPorNombre;
    }

    public boolean isAutomatica() {
        return automatica;
    }
}

package com.aitsolutions.crm.planservicio;

import java.time.LocalDate;

/**
 * Version ligera de PlanServicioResponse para GET /planes-servicio (listado):
 * sin las sesiones, que solo interesan al abrir el detalle de un plan concreto.
 */
public class PlanServicioResumenResponse {

    private final Long id;
    private final Long pacienteId;
    private final String pacienteNombreCompleto;
    private final String tipoServicioNombre;
    private final String subServicioNombre;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final EstadoPlanServicio estado;

    public PlanServicioResumenResponse(PlanServicio plan) {
        this.id = plan.getId();
        this.pacienteId = plan.getPaciente().getId();
        this.pacienteNombreCompleto = plan.getPaciente().getNombre() + " " + plan.getPaciente().getApellidos();
        this.tipoServicioNombre = plan.getTipoServicio().getNombre();
        this.subServicioNombre = plan.getSubServicio() != null ? plan.getSubServicio().getNombre() : null;
        this.fechaInicio = plan.getFechaInicio();
        this.fechaFin = plan.getFechaFin();
        this.estado = plan.getEstado();
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

    public String getTipoServicioNombre() {
        return tipoServicioNombre;
    }

    public String getSubServicioNombre() {
        return subServicioNombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public EstadoPlanServicio getEstado() {
        return estado;
    }
}

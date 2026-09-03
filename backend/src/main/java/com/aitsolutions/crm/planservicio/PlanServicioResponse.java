package com.aitsolutions.crm.planservicio;

import com.aitsolutions.crm.sesion.SesionProgramada;
import com.aitsolutions.crm.sesion.SesionProgramadaResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class PlanServicioResponse {

    private final Long id;
    private final Long pacienteId;
    private final String pacienteNombreCompleto;
    private final Long tipoServicioId;
    private final String tipoServicioNombre;
    private final Long subServicioId;
    private final String subServicioNombre;
    private final Set<DayOfWeek> diasSemana;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final EstadoPlanServicio estado;
    private final List<SesionProgramadaResponse> sesiones;

    public PlanServicioResponse(PlanServicio plan, List<SesionProgramada> sesiones) {
        this.id = plan.getId();
        this.pacienteId = plan.getPaciente().getId();
        this.pacienteNombreCompleto = plan.getPaciente().getNombre() + " " + plan.getPaciente().getApellidos();
        this.tipoServicioId = plan.getTipoServicio().getId();
        this.tipoServicioNombre = plan.getTipoServicio().getNombre();
        this.subServicioId = plan.getSubServicio() != null ? plan.getSubServicio().getId() : null;
        this.subServicioNombre = plan.getSubServicio() != null ? plan.getSubServicio().getNombre() : null;
        this.diasSemana = plan.getDiasSemana();
        this.fechaInicio = plan.getFechaInicio();
        this.fechaFin = plan.getFechaFin();
        this.estado = plan.getEstado();
        this.sesiones = sesiones.stream().map(SesionProgramadaResponse::new).toList();
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

    public Long getTipoServicioId() {
        return tipoServicioId;
    }

    public String getTipoServicioNombre() {
        return tipoServicioNombre;
    }

    public Long getSubServicioId() {
        return subServicioId;
    }

    public String getSubServicioNombre() {
        return subServicioNombre;
    }

    public Set<DayOfWeek> getDiasSemana() {
        return diasSemana;
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

    public List<SesionProgramadaResponse> getSesiones() {
        return sesiones;
    }
}

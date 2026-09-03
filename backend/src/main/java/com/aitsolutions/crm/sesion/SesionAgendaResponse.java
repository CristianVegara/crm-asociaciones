package com.aitsolutions.crm.sesion;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pensado para GET /sesiones (vista de agenda de recepcion): a diferencia de
 * SesionProgramadaResponse (que va anidado dentro de un PlanServicioResponse, que ya
 * aporta el contexto del paciente), aqui cada fila necesita su propio contexto porque
 * la agenda mezcla sesiones de distintos pacientes y planes.
 */
public class SesionAgendaResponse {

    private final Long id;
    private final Long planServicioId;
    private final Long pacienteId;
    private final String pacienteNombreCompleto;
    private final String tipoServicioNombre;
    private final String subServicioNombre;
    private final LocalDate fechaPrevista;
    private final EstadoSesion estado;
    private final Long registradoPorId;
    private final LocalDateTime fechaRegistro;

    public SesionAgendaResponse(SesionProgramada sesion) {
        var plan = sesion.getPlanServicio();
        this.id = sesion.getId();
        this.planServicioId = plan.getId();
        this.pacienteId = plan.getPaciente().getId();
        this.pacienteNombreCompleto = plan.getPaciente().getNombre() + " " + plan.getPaciente().getApellidos();
        this.tipoServicioNombre = plan.getTipoServicio().getNombre();
        this.subServicioNombre = plan.getSubServicio() != null ? plan.getSubServicio().getNombre() : null;
        this.fechaPrevista = sesion.getFechaPrevista();
        this.estado = sesion.getEstado();
        this.registradoPorId = sesion.getRegistradoPor() != null ? sesion.getRegistradoPor().getId() : null;
        this.fechaRegistro = sesion.getFechaRegistro();
    }

    public Long getId() {
        return id;
    }

    public Long getPlanServicioId() {
        return planServicioId;
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

    public LocalDate getFechaPrevista() {
        return fechaPrevista;
    }

    public EstadoSesion getEstado() {
        return estado;
    }

    public Long getRegistradoPorId() {
        return registradoPorId;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}

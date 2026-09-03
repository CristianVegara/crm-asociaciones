package com.aitsolutions.crm.sesion;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SesionProgramadaResponse {

    private final Long id;
    private final Long planServicioId;
    private final LocalDate fechaPrevista;
    private final EstadoSesion estado;
    private final Long registradoPorId;
    private final LocalDateTime fechaRegistro;

    public SesionProgramadaResponse(SesionProgramada sesion) {
        this.id = sesion.getId();
        this.planServicioId = sesion.getPlanServicio().getId();
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

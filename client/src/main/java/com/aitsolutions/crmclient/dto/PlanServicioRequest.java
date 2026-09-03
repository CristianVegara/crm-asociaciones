package com.aitsolutions.crmclient.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public class PlanServicioRequest {
    private Long pacienteId;
    private Long tipoServicioId;
    private Long subServicioId;
    private Set<DayOfWeek> diasSemana;
    private String fechaInicio;
    private String fechaFin;
    private Integer duracionSemanas;

    public PlanServicioRequest(Long pacienteId, Long tipoServicioId, Long subServicioId,
                               Set<DayOfWeek> diasSemana, LocalDate fechaInicio,
                               LocalDate fechaFin, Integer duracionSemanas) {
        this.pacienteId = pacienteId;
        this.tipoServicioId = tipoServicioId;
        this.subServicioId = subServicioId;
        this.diasSemana = diasSemana;
        this.fechaInicio = fechaInicio == null ? null : fechaInicio.toString();
        this.fechaFin = fechaFin == null ? null : fechaFin.toString();
        this.duracionSemanas = duracionSemanas;
    }
    public Long getPacienteId() { return pacienteId; }
    public Long getTipoServicioId() { return tipoServicioId; }
    public Long getSubServicioId() { return subServicioId; }
    public Set<DayOfWeek> getDiasSemana() { return diasSemana; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public Integer getDuracionSemanas() { return duracionSemanas; }
}

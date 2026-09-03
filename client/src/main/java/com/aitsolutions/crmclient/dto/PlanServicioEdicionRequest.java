package com.aitsolutions.crmclient.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public class PlanServicioEdicionRequest {
    private Set<DayOfWeek> diasSemana;
    private String fechaFin;
    private Integer duracionSemanas;

    public PlanServicioEdicionRequest(Set<DayOfWeek> diasSemana, LocalDate fechaFin, Integer duracionSemanas) {
        this.diasSemana = diasSemana;
        this.fechaFin = fechaFin == null ? null : fechaFin.toString();
        this.duracionSemanas = duracionSemanas;
    }
    public Set<DayOfWeek> getDiasSemana() { return diasSemana; }
    public String getFechaFin() { return fechaFin; }
    public Integer getDuracionSemanas() { return duracionSemanas; }
}

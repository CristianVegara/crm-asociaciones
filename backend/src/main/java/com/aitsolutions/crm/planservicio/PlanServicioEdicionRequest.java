package com.aitsolutions.crm.planservicio;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Solo los campos editables tras la creacion (apartado 11 del plan): dias de la semana
 * y la duracion del periodo. El paciente, tipo de servicio y fecha de inicio no cambian
 * una vez creado el plan (si hiciera falta, se finaliza y se crea uno nuevo).
 */
public class PlanServicioEdicionRequest {

    @NotEmpty(message = "Hay que indicar al menos un día de la semana")
    private Set<DayOfWeek> diasSemana;

    private LocalDate fechaFin;

    @Positive(message = "La duración debe ser mayor que cero semanas")
    private Integer duracionSemanas;

    public Set<DayOfWeek> getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(Set<DayOfWeek> diasSemana) {
        this.diasSemana = diasSemana;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getDuracionSemanas() {
        return duracionSemanas;
    }

    public void setDuracionSemanas(Integer duracionSemanas) {
        this.duracionSemanas = duracionSemanas;
    }
}

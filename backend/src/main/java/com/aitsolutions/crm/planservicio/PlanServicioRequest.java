package com.aitsolutions.crm.planservicio;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * fechaFin y duracionSemanas son mutuamente excluyentes: se debe indicar exactamente uno
 * de los dos (decision tomada porque el plan original dejaba abierto "fecha fin / duracion").
 * La validacion de que se cumpla se hace en el servicio, no aqui, porque involucra a los
 * dos campos a la vez (Bean Validation estandar no lo expresa bien con anotaciones simples).
 */
public class PlanServicioRequest {

    @NotNull(message = "El paciente es obligatorio")
    private Long pacienteId;

    @NotNull(message = "El tipo de servicio es obligatorio")
    private Long tipoServicioId;

    // Opcional: un plan puede no concretar subservicio.
    private Long subServicioId;

    @NotEmpty(message = "Hay que indicar al menos un día de la semana")
    private Set<DayOfWeek> diasSemana;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @Positive(message = "La duración debe ser mayor que cero semanas")
    private Integer duracionSemanas;

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getTipoServicioId() {
        return tipoServicioId;
    }

    public void setTipoServicioId(Long tipoServicioId) {
        this.tipoServicioId = tipoServicioId;
    }

    public Long getSubServicioId() {
        return subServicioId;
    }

    public void setSubServicioId(Long subServicioId) {
        this.subServicioId = subServicioId;
    }

    public Set<DayOfWeek> getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(Set<DayOfWeek> diasSemana) {
        this.diasSemana = diasSemana;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
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

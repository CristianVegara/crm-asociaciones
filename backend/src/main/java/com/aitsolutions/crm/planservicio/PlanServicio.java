package com.aitsolutions.crm.planservicio;

import com.aitsolutions.crm.paciente.Paciente;
import com.aitsolutions.crm.tiposervicio.SubServicio;
import com.aitsolutions.crm.tiposervicio.TipoServicio;
import com.aitsolutions.crm.trabajador.Trabajador;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Ej. "Ana Valero, rehabilitacion, 2 veces por semana, 3 meses" (apartado 4-5 del plan).
 * Al crearse (o editarse: apartado 11) genera/regenera su calendario de SesionProgramada.
 */
@Entity
@Table(name = "plan_servicio")
public class PlanServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servicio_id", nullable = false)
    private TipoServicio tipoServicio;

    // Opcional: un plan puede no concretar subservicio (apartado 4 del plan).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_servicio_id")
    private SubServicio subServicio;

    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_servicio_dia_semana", joinColumns = @JoinColumn(name = "plan_servicio_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana")
    private Set<DayOfWeek> diasSemana = new HashSet<>();

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPlanServicio estado = EstadoPlanServicio.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Trabajador creadoPor;

    // Distinto de fechaInicio (que es cuando EMPIEZAN las sesiones): esto es cuando se dio
    // de alta el plan en el sistema. Lo usa el informe del paso 8.
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    protected PlanServicio() {
        // Requerido por JPA
    }

    public PlanServicio(Paciente paciente, TipoServicio tipoServicio, SubServicio subServicio,
                         Set<DayOfWeek> diasSemana, LocalDate fechaInicio, LocalDate fechaFin,
                         Trabajador creadoPor) {
        this.paciente = paciente;
        this.tipoServicio = tipoServicio;
        this.subServicio = subServicio;
        this.diasSemana = diasSemana;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.creadoPor = creadoPor;
    }

    public Long getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public SubServicio getSubServicio() {
        return subServicio;
    }

    public void setSubServicio(SubServicio subServicio) {
        this.subServicio = subServicio;
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

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoPlanServicio getEstado() {
        return estado;
    }

    public void setEstado(EstadoPlanServicio estado) {
        this.estado = estado;
    }

    public Trabajador getCreadoPor() {
        return creadoPor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }
}

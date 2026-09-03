package com.aitsolutions.crm.sesion;

import com.aitsolutions.crm.planservicio.PlanServicio;
import com.aitsolutions.crm.trabajador.Trabajador;
import jakarta.persistence.Column;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesion_programada")
public class SesionProgramada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_servicio_id", nullable = false)
    private PlanServicio planServicio;

    @Column(name = "fecha_prevista", nullable = false)
    private LocalDate fechaPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSesion estado = EstadoSesion.PENDIENTE;

    // Se rellenan al marcar la sesion (paso 5), no al generarla.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por")
    private Trabajador registradoPor;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    protected SesionProgramada() {
        // Requerido por JPA
    }

    public SesionProgramada(PlanServicio planServicio, LocalDate fechaPrevista) {
        this.planServicio = planServicio;
        this.fechaPrevista = fechaPrevista;
    }

    public Long getId() {
        return id;
    }

    public PlanServicio getPlanServicio() {
        return planServicio;
    }

    public LocalDate getFechaPrevista() {
        return fechaPrevista;
    }

    public EstadoSesion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSesion estado) {
        this.estado = estado;
    }

    public Trabajador getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Trabajador registradoPor) {
        this.registradoPor = registradoPor;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}

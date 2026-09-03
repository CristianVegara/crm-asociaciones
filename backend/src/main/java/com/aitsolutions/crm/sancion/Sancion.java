package com.aitsolutions.crm.sancion;

import com.aitsolutions.crm.paciente.Paciente;
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

@Entity
@Table(name = "sancion")
public class Sancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    // Opcional (apartado 4 del plan): una sancion puede no estar ligada a un plan concreto.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_servicio_id")
    private PlanServicio planServicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSancion tipo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicada_por", nullable = false)
    private Trabajador aplicadaPor;

    // Distingue las generadas por las reglas automaticas (apartado 4) de las manuales,
    // util para auditoria/informes sin tener que fijarse en el texto del motivo.
    @Column(nullable = false)
    private boolean automatica;

    protected Sancion() {
        // Requerido por JPA
    }

    public Sancion(Paciente paciente, PlanServicio planServicio, TipoSancion tipo,
                   String motivo, Trabajador aplicadaPor, boolean automatica) {
        this.paciente = paciente;
        this.planServicio = planServicio;
        this.tipo = tipo;
        this.fecha = LocalDate.now();
        this.motivo = motivo;
        this.aplicadaPor = aplicadaPor;
        this.automatica = automatica;
    }

    public Long getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public PlanServicio getPlanServicio() {
        return planServicio;
    }

    public TipoSancion getTipo() {
        return tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public Trabajador getAplicadaPor() {
        return aplicadaPor;
    }

    public boolean isAutomatica() {
        return automatica;
    }
}

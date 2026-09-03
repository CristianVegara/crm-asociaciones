package com.aitsolutions.crm.informe;

import com.aitsolutions.crm.trabajador.Trabajador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "informe_generado")
public class InformeGenerado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate desde;

    @Column(nullable = false)
    private LocalDate hasta;

    private String periodo;

    @Column(name = "tipo_informe", nullable = false)
    private String tipoInforme;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "generado_por", nullable = false)
    private Trabajador generadoPor;

    protected InformeGenerado() {
    }

    public InformeGenerado(LocalDate desde, LocalDate hasta, String periodo, String tipoInforme, Trabajador generadoPor) {
        this.desde = desde;
        this.hasta = hasta;
        this.periodo = periodo;
        this.tipoInforme = tipoInforme;
        this.fechaGeneracion = LocalDateTime.now();
        this.generadoPor = generadoPor;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public String getPeriodo() {
        return periodo;
    }

    public String getTipoInforme() {
        return tipoInforme;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public Trabajador getGeneradoPor() {
        return generadoPor;
    }
}

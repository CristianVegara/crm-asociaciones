package com.aitsolutions.crm.tiposervicio;

import com.aitsolutions.crm.rol.Rol;
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
import jakarta.persistence.UniqueConstraint;

/**
 * Fila de la matriz "qué rol puede hacer qué en cada tipo de servicio" (apartado 4 del plan).
 * Configurable por el director sin tocar código, vía PUT /tipos-servicio/{id}/responsables.
 */
@Entity
@Table(name = "tipo_servicio_responsable",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tipo_servicio_id", "rol_id", "capacidad"}))
public class TipoServicioResponsable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servicio_id", nullable = false)
    private TipoServicio tipoServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CapacidadServicio capacidad;

    protected TipoServicioResponsable() {
        // Requerido por JPA
    }

    public TipoServicioResponsable(TipoServicio tipoServicio, Rol rol, CapacidadServicio capacidad) {
        this.tipoServicio = tipoServicio;
        this.rol = rol;
        this.capacidad = capacidad;
    }

    public Long getId() {
        return id;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public Rol getRol() {
        return rol;
    }

    public CapacidadServicio getCapacidad() {
        return capacidad;
    }
}

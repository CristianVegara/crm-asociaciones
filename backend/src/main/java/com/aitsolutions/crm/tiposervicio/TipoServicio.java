package com.aitsolutions.crm.tiposervicio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_servicio")
public class TipoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String icono;

    private String color;

    @Column(nullable = false)
    private boolean activo = true;

    // orphanRemoval: si se quita un subservicio de esta lista y se guarda el tipo, se borra.
    @OneToMany(mappedBy = "tipoServicio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SubServicio> subServicios = new ArrayList<>();

    protected TipoServicio() {
        // Requerido por JPA
    }

    public TipoServicio(String nombre, String icono, String color) {
        this.nombre = nombre;
        this.icono = icono;
        this.color = color;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<SubServicio> getSubServicios() {
        return subServicios;
    }

    public void agregarSubServicio(SubServicio subServicio) {
        subServicios.add(subServicio);
        subServicio.setTipoServicio(this);
    }
}

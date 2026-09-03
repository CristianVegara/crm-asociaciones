package com.aitsolutions.crm.paciente;

import com.aitsolutions.crm.asociacion.Asociacion;
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

@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(name = "numero_expediente", nullable = false, unique = true)
    private String numeroExpediente;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String genero;

    @Column(name = "dni", unique = true)
    private String dni;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "email")
    private String email;

    @Column(nullable = false)
    private boolean activo = true;

    // Fecha de alta en el sistema (distinta de fechaNacimiento): la usa el informe del
    // paso 8 para calcular inscripciones nuevas en un periodo.
    @Column(name = "fecha_alta", nullable = false)
    private LocalDate fechaAlta = LocalDate.now();

    // 1 paciente -> 1 asociacion, tal y como fija el apartado 4 del plan.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asociacion_id", nullable = false)
    private Asociacion asociacion;

    protected Paciente() {
        // Requerido por JPA
    }

    public Paciente(String nombre, String apellidos, String numeroExpediente,
                     LocalDate fechaNacimiento, String genero, Asociacion asociacion) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.numeroExpediente = numeroExpediente;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.asociacion = asociacion;
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

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public void setNumeroExpediente(String numeroExpediente) {
        this.numeroExpediente = numeroExpediente;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public Asociacion getAsociacion() {
        return asociacion;
    }

    public void setAsociacion(Asociacion asociacion) {
        this.asociacion = asociacion;
    }
}

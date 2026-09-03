package com.aitsolutions.crm.paciente;

import java.time.LocalDate;

/**
 * NOTA: segun el apartado 6 del plan, GET /pacientes/{id} debera incluir tambien
 * planes de servicio, sesiones y sanciones. Se añadira cuando existan esos modulos
 * (pasos 4 y 6 del orden de construccion); por ahora solo expone los datos propios.
 */
public class PacienteResponse {

    private final Long id;
    private final String nombre;
    private final String apellidos;
    private final String numeroExpediente;
    private final LocalDate fechaNacimiento;
    private final LocalDate fechaAlta;
    private final String genero;
    private final String dni;
    private final String telefono;
    private final String email;
    private final boolean activo;
    private final Long asociacionId;
    private final String asociacionNombre;

    public PacienteResponse(Paciente paciente) {
        this.id = paciente.getId();
        this.nombre = paciente.getNombre();
        this.apellidos = paciente.getApellidos();
        this.numeroExpediente = paciente.getNumeroExpediente();
        this.fechaNacimiento = paciente.getFechaNacimiento();
        this.fechaAlta = paciente.getFechaAlta();
        this.genero = paciente.getGenero();
        this.dni = paciente.getDni();
        this.telefono = paciente.getTelefono();
        this.email = paciente.getEmail();
        this.activo = paciente.isActivo();
        this.asociacionId = paciente.getAsociacion().getId();
        this.asociacionNombre = paciente.getAsociacion().getNombre();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public String getGenero() {
        return genero;
    }
    public String getDni() { return dni; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }

    public boolean isActivo() {
        return activo;
    }

    public Long getAsociacionId() {
        return asociacionId;
    }

    public String getAsociacionNombre() {
        return asociacionNombre;
    }
}

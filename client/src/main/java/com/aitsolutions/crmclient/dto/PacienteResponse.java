package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PacienteResponse {

    private Long id;
    private String nombre;
    private String apellidos;
    private String numeroExpediente;
    private String fechaNacimiento;
    private String genero;
    private String dni;
    private String telefono;
    private String email;
    private boolean activo;
    private Long asociacionId;
    private String asociacionNombre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
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

    public Long getAsociacionId() {
        return asociacionId;
    }

    public void setAsociacionId(Long asociacionId) {
        this.asociacionId = asociacionId;
    }

    public String getAsociacionNombre() {
        return asociacionNombre;
    }

    public void setAsociacionNombre(String asociacionNombre) {
        this.asociacionNombre = asociacionNombre;
    }

    @Override
    public String toString() {
        return nombre + " " + apellidos + " (" + numeroExpediente + ")";
    }
}

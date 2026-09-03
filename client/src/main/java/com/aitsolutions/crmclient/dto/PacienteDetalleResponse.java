package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PacienteDetalleResponse {
    private Long id;
    private String nombre;
    private String apellidos;
    private String numeroExpediente;
    private String fechaNacimiento;
    private String fechaAlta;
    private String genero;
    private String dni;
    private String telefono;
    private String email;
    private boolean activo;
    private Long asociacionId;
    private String asociacionNombre;
    private List<PlanServicioResponse> planes = new ArrayList<>();
    private List<SesionProgramadaResponse> sesiones = new ArrayList<>();
    private List<SancionResponse> sanciones = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String value) { nombre = value; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String value) { apellidos = value; }
    public String getNumeroExpediente() { return numeroExpediente; }
    public void setNumeroExpediente(String value) { numeroExpediente = value; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String value) { fechaNacimiento = value; }
    public String getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(String value) { fechaAlta = value; }
    public String getGenero() { return genero; }
    public void setGenero(String value) { genero = value; }
    public String getDni() { return dni; }
    public void setDni(String value) { dni = value; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String value) { telefono = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean value) { activo = value; }
    public Long getAsociacionId() { return asociacionId; }
    public void setAsociacionId(Long value) { asociacionId = value; }
    public String getAsociacionNombre() { return asociacionNombre; }
    public void setAsociacionNombre(String value) { asociacionNombre = value; }
    public List<PlanServicioResponse> getPlanes() { return planes; }
    public void setPlanes(List<PlanServicioResponse> planes) { this.planes = planes; }
    public List<SesionProgramadaResponse> getSesiones() { return sesiones; }
    public void setSesiones(List<SesionProgramadaResponse> sesiones) { this.sesiones = sesiones; }
    public List<SancionResponse> getSanciones() { return sanciones; }
    public void setSanciones(List<SancionResponse> sanciones) { this.sanciones = sanciones; }
}

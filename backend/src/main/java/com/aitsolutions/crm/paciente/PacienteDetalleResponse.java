package com.aitsolutions.crm.paciente;

import com.aitsolutions.crm.planservicio.PlanServicioResponse;
import com.aitsolutions.crm.sancion.SancionResponse;
import com.aitsolutions.crm.sesion.SesionProgramadaResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Datos de la ficha del paciente. Se mantiene separado del DTO del listado para
 * no cargar planes y sesiones al consultar una pagina de pacientes.
 */
public class PacienteDetalleResponse {

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
    private final List<PlanServicioResponse> planes;
    private final List<SesionProgramadaResponse> sesiones;
    private final List<SancionResponse> sanciones;

    public PacienteDetalleResponse(Paciente paciente, List<PlanServicioResponse> planes,
                                   List<SesionProgramadaResponse> sesiones, List<SancionResponse> sanciones) {
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
        this.planes = planes;
        this.sesiones = sesiones;
        this.sanciones = sanciones;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getNumeroExpediente() { return numeroExpediente; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public LocalDate getFechaAlta() { return fechaAlta; }
    public String getGenero() { return genero; }
    public String getDni() { return dni; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public boolean isActivo() { return activo; }
    public Long getAsociacionId() { return asociacionId; }
    public String getAsociacionNombre() { return asociacionNombre; }
    public List<PlanServicioResponse> getPlanes() { return planes; }
    public List<SesionProgramadaResponse> getSesiones() { return sesiones; }
    public List<SancionResponse> getSanciones() { return sanciones; }
}

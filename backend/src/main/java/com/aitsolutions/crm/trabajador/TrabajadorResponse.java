package com.aitsolutions.crm.trabajador;

public class TrabajadorResponse {

    private final Long id;
    private final String nombre;
    private final String apellidos;
    private final String usuario;
    private final boolean activo;
    private final Long rolId;
    private final String rolNombre;

    public TrabajadorResponse(Trabajador trabajador) {
        this.id = trabajador.getId();
        this.nombre = trabajador.getNombre();
        this.apellidos = trabajador.getApellidos();
        this.usuario = trabajador.getUsuario();
        this.activo = trabajador.isActivo();
        this.rolId = trabajador.getRol().getId();
        this.rolNombre = trabajador.getRol().getNombre();
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

    public String getUsuario() {
        return usuario;
    }

    public boolean isActivo() {
        return activo;
    }

    public Long getRolId() {
        return rolId;
    }

    public String getRolNombre() {
        return rolNombre;
    }
}

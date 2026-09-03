package com.aitsolutions.crm.auth;

import com.aitsolutions.crm.permiso.Permiso;
import com.aitsolutions.crm.trabajador.Trabajador;

import java.util.Set;

public class LoginResponse {

    private final String token;
    private final String tipo = "Bearer";
    private final long expiraEnSegundos;
    private final Long trabajadorId;
    private final String nombreCompleto;
    private final String rolNombre;
    private final Set<Permiso> permisos;

    public LoginResponse(String token, long expiraEnSegundos, Trabajador trabajador) {
        this.token = token;
        this.expiraEnSegundos = expiraEnSegundos;
        this.trabajadorId = trabajador.getId();
        this.nombreCompleto = trabajador.getNombre() + " " + trabajador.getApellidos();
        this.rolNombre = trabajador.getRol().getNombre();
        this.permisos = trabajador.getRol().getPermisos();
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public long getExpiraEnSegundos() {
        return expiraEnSegundos;
    }

    public Long getTrabajadorId() {
        return trabajadorId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public Set<Permiso> getPermisos() {
        return permisos;
    }
}

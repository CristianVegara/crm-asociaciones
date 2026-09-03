package com.aitsolutions.crmclient.sesion;

import com.aitsolutions.crmclient.dto.LoginResponse;

import java.util.Set;

/**
 * Guarda en memoria el token y los datos del trabajador mientras dura la sesion del cliente.
 * Cliente de escritorio de un unico usuario a la vez: un singleton sencillo es suficiente,
 * no hace falta nada mas elaborado.
 */
public class SesionActiva {

    private static SesionActiva instancia;

    private String token;
    private Long trabajadorId;
    private String nombreCompleto;
    private String rolNombre;
    private Set<String> permisos;

    private SesionActiva() {
    }

    public static synchronized SesionActiva getInstance() {
        if (instancia == null) {
            instancia = new SesionActiva();
        }
        return instancia;
    }

    public void iniciar(LoginResponse loginResponse) {
        this.token = loginResponse.getToken();
        this.trabajadorId = loginResponse.getTrabajadorId();
        this.nombreCompleto = loginResponse.getNombreCompleto();
        this.rolNombre = loginResponse.getRolNombre();
        this.permisos = loginResponse.getPermisos();
    }

    public void cerrar() {
        this.token = null;
        this.trabajadorId = null;
        this.nombreCompleto = null;
        this.rolNombre = null;
        this.permisos = null;
    }

    public boolean estaAutenticado() {
        return token != null;
    }

    public boolean tienePermiso(String permiso) {
        return permisos != null && permisos.contains(permiso);
    }

    public String getToken() {
        return token;
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

    public Set<String> getPermisos() {
        return permisos;
    }
}

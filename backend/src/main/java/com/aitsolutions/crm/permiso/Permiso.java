package com.aitsolutions.crm.permiso;

/**
 * Catalogo fijo de capacidades del sistema (apartado 4 del plan).
 * No se gestiona en base de datos: se amplia via codigo y despliegue, no desde la UI.
 * Los roles se asignan un subconjunto de estos permisos (ver Rol.permisos).
 */
public enum Permiso {

    GESTIONAR_TRABAJADORES("Alta, edicion y baja de trabajadores y roles"),
    GESTIONAR_CATALOGO_SERVICIOS("Alta y edicion de tipos de servicio y subservicios"),
    VER_INFORMES("Acceso al modulo de informes"),
    GESTIONAR_PACIENTES("Alta y edicion de pacientes y asociaciones"),
    CREAR_PLAN_SERVICIO("Alta de planes de servicio para un paciente"),
    REGISTRAR_ASISTENCIA("Marcar el estado de una sesion programada"),
    APLICAR_SANCION("Aplicar una sancion a un paciente");

    private final String descripcion;

    Permiso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

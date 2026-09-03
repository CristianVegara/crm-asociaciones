package com.aitsolutions.crm.tiposervicio;

/**
 * Las tres capacidades del apartado 4 del plan: qué puede hacer un rol en un tipo de
 * servicio concreto. Ej: el rol "Recepción" tiene REGISTRAR_ASISTENCIA en Rehabilitación;
 * el rol "Rehabilitador" tiene APLICAR_SANCION en ese mismo servicio.
 */
public enum CapacidadServicio {
    REGISTRAR_ASISTENCIA,
    APLICAR_SANCION,
    GESTIONAR_PLAN
}

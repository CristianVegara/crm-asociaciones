package com.aitsolutions.crm.paciente;

import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros opcionales y combinables de GET /pacientes?asociacionId=&nombre=
 * (apartado 6 del plan). Cada metodo devuelve null si el filtro no aplica,
 * y Specification.where() ignora los filtros nulos al combinarlos.
 */
public class PacienteSpecifications {

    private PacienteSpecifications() {
        // Clase de utilidades, no instanciable
    }

    public static Specification<Paciente> conAsociacion(Long asociacionId) {
        if (asociacionId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("asociacion").get("id"), asociacionId);
    }

    public static Specification<Paciente> conNombreQueContenga(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        String patron = "%" + nombre.toLowerCase() + "%";
        // Busca coincidencia parcial tanto en nombre como en apellidos.
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(cb.lower(root.get("apellidos")), patron)
        );
    }
}

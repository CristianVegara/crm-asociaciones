package com.aitsolutions.crm.planservicio;

import org.springframework.data.jpa.domain.Specification;

public class PlanServicioSpecifications {

    private PlanServicioSpecifications() {
        // Clase de utilidades, no instanciable
    }

    public static Specification<PlanServicio> conPaciente(Long pacienteId) {
        if (pacienteId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("paciente").get("id"), pacienteId);
    }

    public static Specification<PlanServicio> conTipoServicio(Long tipoServicioId) {
        if (tipoServicioId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("tipoServicio").get("id"), tipoServicioId);
    }

    public static Specification<PlanServicio> conEstado(EstadoPlanServicio estado) {
        if (estado == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }
}

package com.aitsolutions.crm.sesion;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class SesionProgramadaSpecifications {

    private SesionProgramadaSpecifications() {
        // Clase de utilidades, no instanciable
    }

    public static Specification<SesionProgramada> conPlan(Long planServicioId) {
        if (planServicioId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("planServicio").get("id"), planServicioId);
    }

    public static Specification<SesionProgramada> conFechaDesde(LocalDate desde) {
        if (desde == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaPrevista"), desde);
    }

    public static Specification<SesionProgramada> conFechaHasta(LocalDate hasta) {
        if (hasta == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaPrevista"), hasta);
    }

    public static Specification<SesionProgramada> conEstado(EstadoSesion estado) {
        if (estado == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }
}

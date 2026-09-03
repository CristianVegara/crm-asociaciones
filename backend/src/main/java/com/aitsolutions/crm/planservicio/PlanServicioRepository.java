package com.aitsolutions.crm.planservicio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface PlanServicioRepository extends JpaRepository<PlanServicio, Long>, JpaSpecificationExecutor<PlanServicio> {

    // Usados por el informe (paso 8).
    long countByFechaCreacionBetween(LocalDateTime desde, LocalDateTime hasta);

    long countByFechaFinalizacionBetween(LocalDateTime desde, LocalDateTime hasta);

    List<PlanServicio> findByFechaCreacionBetween(LocalDateTime desde, LocalDateTime hasta);
}

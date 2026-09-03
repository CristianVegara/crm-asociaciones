package com.aitsolutions.crm.sesion;

import com.aitsolutions.crm.planservicio.PlanServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SesionProgramadaRepository extends JpaRepository<SesionProgramada, Long>, JpaSpecificationExecutor<SesionProgramada> {

    List<SesionProgramada> findByPlanServicioOrderByFechaPrevistaAsc(PlanServicio planServicio);

    Optional<SesionProgramada> findByPlanServicioAndFechaPrevista(PlanServicio planServicio, LocalDate fechaPrevista);

    // Usado al editar un plan (apartado 11): solo se tocan sesiones futuras que nadie
    // ha marcado todavia. Las ya registradas (VERDE/NARANJA/ROJO/AMARILLO) son historial
    // y nunca se borran, sea cual sea su fecha.
    List<SesionProgramada> findByPlanServicioAndEstadoAndFechaPrevistaGreaterThanEqual(
            PlanServicio planServicio, EstadoSesion estado, LocalDate fecha);

    // Usado por las reglas automaticas de sancion (apartado 4 del plan): 3 ROJAS o 6 NARANJAS
    // en el historico de un plan concreto.
    long countByPlanServicioAndEstado(PlanServicio planServicio, EstadoSesion estado);

    // Usado por el informe (paso 8): todas las sesiones previstas en un rango de fechas,
    // independientemente del plan al que pertenezcan.
    List<SesionProgramada> findByFechaPrevistaBetween(LocalDate desde, LocalDate hasta);
}

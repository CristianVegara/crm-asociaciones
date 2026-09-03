package com.aitsolutions.crm.paciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;

public interface PacienteRepository extends JpaRepository<Paciente, Long>, JpaSpecificationExecutor<Paciente> {

    boolean existsByNumeroExpediente(String numeroExpediente);

    // Usados por el informe (paso 8).
    long countByActivoTrue();

    long countByFechaAltaBetween(LocalDate desde, LocalDate hasta);
}

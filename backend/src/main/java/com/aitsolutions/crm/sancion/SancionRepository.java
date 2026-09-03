package com.aitsolutions.crm.sancion;

import com.aitsolutions.crm.paciente.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SancionRepository extends JpaRepository<Sancion, Long> {

    List<Sancion> findByPacienteOrderByFechaDesc(Paciente paciente);

    // Usado por el informe (paso 8).
    List<Sancion> findByFechaBetween(LocalDate desde, LocalDate hasta);
}

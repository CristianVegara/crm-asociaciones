package com.aitsolutions.crm.informe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InformeGeneradoRepository extends JpaRepository<InformeGenerado, Long> {

    List<InformeGenerado> findAllByOrderByFechaGeneracionDesc();
}

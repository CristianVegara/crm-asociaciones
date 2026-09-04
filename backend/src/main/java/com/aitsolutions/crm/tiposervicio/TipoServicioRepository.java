package com.aitsolutions.crm.tiposervicio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoServicioRepository extends JpaRepository<TipoServicio, Long> {
    Optional<TipoServicio> findByNombre(String nombre);
}

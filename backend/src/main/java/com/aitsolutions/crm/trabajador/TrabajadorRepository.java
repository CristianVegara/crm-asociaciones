package com.aitsolutions.crm.trabajador;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {

    // Se usará en el paso 2 (login) para buscar por usuario de acceso.
    Optional<Trabajador> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);
}

package com.aitsolutions.crm.config;

import com.aitsolutions.crm.permiso.Permiso;
import com.aitsolutions.crm.rol.Rol;
import com.aitsolutions.crm.rol.RolRepository;
import com.aitsolutions.crm.trabajador.Trabajador;
import com.aitsolutions.crm.trabajador.TrabajadorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Resuelve el arranque en frio: sin esto, nadie podria crear el primer trabajador porque
 * POST /trabajadores ya exige el permiso GESTIONAR_TRABAJADORES (paso 2 del plan).
 * Solo actua si la tabla de trabajadores esta vacia, asi que no interfiere una vez la
 * asociacion ya tiene su propio director dado de alta.
 */
@Component
public class InicializacionDatos implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializacionDatos.class);
    private static final String USUARIO_INICIAL = "admin";
    private static final String PASSWORD_INICIAL = "admin1234";

    private final RolRepository rolRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final PasswordEncoder passwordEncoder;

    public InicializacionDatos(RolRepository rolRepository,
                                TrabajadorRepository trabajadorRepository,
                                PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (trabajadorRepository.count() > 0) {
            return;
        }

        Rol director = rolRepository.findByNombre("Director")
                .orElseGet(() -> new Rol(
                        "Director",
                        "Rol inicial con todos los permisos del sistema"
                ));
        director.setPermisos(Set.of(Permiso.values()));
        rolRepository.save(director);

        Trabajador admin = new Trabajador(
                "Admin",
                "Inicial",
                USUARIO_INICIAL,
                passwordEncoder.encode(PASSWORD_INICIAL),
                director
        );
        trabajadorRepository.save(admin);

        log.warn("Trabajador inicial creado -> usuario: '{}', contraseña: '{}'. "
                + "Cambia esta contraseña en cuanto entres (PUT /trabajadores/{{id}}).",
                USUARIO_INICIAL, PASSWORD_INICIAL);
    }
}

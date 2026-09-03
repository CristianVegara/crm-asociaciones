package com.aitsolutions.crm.auth;

import com.aitsolutions.crm.trabajador.Trabajador;
import com.aitsolutions.crm.trabajador.TrabajadorRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * El JWT solo lleva el "usuario" (login) y los permisos como claims (ver JwtService),
 * no el Trabajador completo. Cuando un modulo necesita saber quien esta haciendo la
 * peticion (creado_por, registrado_por, aplicada_por...) resuelve aqui la unica consulta
 * a base de datos que hace falta.
 */
@Component
public class UsuarioAutenticadoService {

    private final TrabajadorRepository trabajadorRepository;

    public UsuarioAutenticadoService(TrabajadorRepository trabajadorRepository) {
        this.trabajadorRepository = trabajadorRepository;
    }

    public Trabajador obtenerTrabajadorActual() {
        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        return trabajadorRepository.findByUsuario(usuario)
                .orElseThrow(() -> new IllegalStateException(
                        "El trabajador autenticado '" + usuario + "' ya no existe en el sistema"));
    }
}

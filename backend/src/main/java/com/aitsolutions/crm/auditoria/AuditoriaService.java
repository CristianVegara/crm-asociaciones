package com.aitsolutions.crm.auditoria;

import com.aitsolutions.crm.auth.UsuarioAutenticadoService;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {
    private final AuditoriaEventoRepository repository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public AuditoriaService(AuditoriaEventoRepository repository,
                            UsuarioAutenticadoService usuarioAutenticadoService) {
        this.repository = repository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    public void registrar(String accion, String detalle) {
        repository.save(AuditoriaEvento.deNegocio(
                usuarioAutenticadoService.obtenerTrabajadorActual().getUsuario(),
                accion, detalle));
    }
}

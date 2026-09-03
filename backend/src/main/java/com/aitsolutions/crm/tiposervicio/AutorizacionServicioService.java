package com.aitsolutions.crm.tiposervicio;

import com.aitsolutions.crm.auth.UsuarioAutenticadoService;
import com.aitsolutions.crm.trabajador.Trabajador;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Complementa los permisos generales (@PreAuthorize a nivel de módulo, ej. REGISTRAR_ASISTENCIA)
 * con la comprobación fina del apartado 4 del plan: el permiso general da acceso al módulo,
 * TipoServicioResponsable decide para CUÁLES tipos de servicio concretos puede actuar el rol.
 * Lanza AccessDeniedException (la misma excepción que usa Spring Security para @PreAuthorize),
 * así que el resultado HTTP es un 403 consistente con el resto de la API sin código extra.
 */
@Component
public class AutorizacionServicioService {

    private final TipoServicioResponsableRepository tipoServicioResponsableRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public AutorizacionServicioService(TipoServicioResponsableRepository tipoServicioResponsableRepository,
                                        UsuarioAutenticadoService usuarioAutenticadoService) {
        this.tipoServicioResponsableRepository = tipoServicioResponsableRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    public void verificarCapacidad(TipoServicio tipoServicio, CapacidadServicio capacidad) {
        Trabajador trabajador = usuarioAutenticadoService.obtenerTrabajadorActual();

        boolean autorizado = tipoServicioResponsableRepository
                .existsByTipoServicioAndRolAndCapacidad(tipoServicio, trabajador.getRol(), capacidad);

        if (!autorizado) {
            throw new AccessDeniedException("Tu rol (" + trabajador.getRol().getNombre()
                    + ") no tiene la capacidad " + capacidad + " en el servicio " + tipoServicio.getNombre());
        }
    }
}

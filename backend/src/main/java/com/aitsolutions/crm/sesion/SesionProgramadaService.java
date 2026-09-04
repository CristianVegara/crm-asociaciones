package com.aitsolutions.crm.sesion;

import com.aitsolutions.crm.auditoria.AuditoriaService;
import com.aitsolutions.crm.auth.UsuarioAutenticadoService;
import com.aitsolutions.crm.common.ResourceNotFoundException;
import com.aitsolutions.crm.sancion.SancionService;
import com.aitsolutions.crm.tiposervicio.AutorizacionServicioService;
import com.aitsolutions.crm.tiposervicio.CapacidadServicio;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SesionProgramadaService {

    private final SesionProgramadaRepository sesionProgramadaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final SancionService sancionService;
    private final AutorizacionServicioService autorizacionServicioService;
    private final AuditoriaService auditoriaService;

    public SesionProgramadaService(SesionProgramadaRepository sesionProgramadaRepository,
                                    UsuarioAutenticadoService usuarioAutenticadoService,
                                    SancionService sancionService,
                                    AutorizacionServicioService autorizacionServicioService,
                                    AuditoriaService auditoriaService) {
        this.sesionProgramadaRepository = sesionProgramadaRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.sancionService = sancionService;
        this.autorizacionServicioService = autorizacionServicioService;
        this.auditoriaService = auditoriaService;
    }

    // Vista de agenda para recepcion (GET /sesiones?planServicioId=&desde=&hasta=&estado=).
    public List<SesionProgramada> buscar(Long planServicioId, LocalDate desde, LocalDate hasta, EstadoSesion estado) {
        Specification<SesionProgramada> filtro = Specification
                .where(SesionProgramadaSpecifications.conPlan(planServicioId))
                .and(SesionProgramadaSpecifications.conFechaDesde(desde))
                .and(SesionProgramadaSpecifications.conFechaHasta(hasta))
                .and(SesionProgramadaSpecifications.conEstado(estado));
        return sesionProgramadaRepository.findAll(filtro, Sort.by(Sort.Direction.ASC, "fechaPrevista"));
    }

    /**
     * Marca una sesion como asistida o con algun tipo de falta (VERDE/NARANJA/ROJO/AMARILLO).
     * No se permite volver a PENDIENTE por esta via: ese estado es solo el punto de partida
     * al generar el calendario, nunca un destino de marcado manual.
     *
     * Tras guardar, evalua las reglas de sancion automatica del apartado 4 del plan
     * (3 ROJAS / 6 NARANJAS en el historico del plan -> Sancion automatica). Ver
     * SancionService.evaluarReglasAutomaticas para el detalle y las decisiones tomadas
     * sobre el alcance (sin conteo de "ciclos" todavia).
     */
    public SesionProgramada marcarEstado(Long id, EstadoSesion nuevoEstado) {
        if (nuevoEstado == EstadoSesion.PENDIENTE) {
            throw new IllegalArgumentException(
                    "PENDIENTE es solo el estado inicial de una sesión, no se puede volver a marcar así");
        }

        SesionProgramada sesion = sesionProgramadaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la sesión con id " + id));

        autorizacionServicioService.verificarCapacidad(
                sesion.getPlanServicio().getTipoServicio(), CapacidadServicio.REGISTRAR_ASISTENCIA);

        sesion.setEstado(nuevoEstado);
        sesion.setRegistradoPor(usuarioAutenticadoService.obtenerTrabajadorActual());
        sesion.setFechaRegistro(LocalDateTime.now());

        sesion = sesionProgramadaRepository.save(sesion);
        sancionService.evaluarReglasAutomaticas(sesion);
        auditoriaService.registrar("SESION_ESTADO", "Sesión " + sesion.getId() + ": " + nuevoEstado);

        return sesion;
    }

    /**
     * Borra una sesion puntual sin afectar al resto del plan (apartado 11 del plan).
     * La sesión se conserva como CANCELADA para mantener la trazabilidad del calendario.
     */
    public void eliminar(Long id) {
        SesionProgramada sesion = sesionProgramadaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la sesión con id " + id));

        autorizacionServicioService.verificarCapacidad(
                sesion.getPlanServicio().getTipoServicio(), CapacidadServicio.GESTIONAR_PLAN);

        if (sesion.getEstado() != EstadoSesion.PENDIENTE) {
            throw new IllegalArgumentException(
                    "Solo se puede cancelar una sesión pendiente; las sesiones registradas forman parte del historial");
        }

        sesion.setEstado(EstadoSesion.CANCELADA);
        sesionProgramadaRepository.save(sesion);
        auditoriaService.registrar("SESION_CANCELADA", "Sesión " + sesion.getId());
    }
}

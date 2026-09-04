package com.aitsolutions.crm.sancion;

import com.aitsolutions.crm.auditoria.AuditoriaService;
import com.aitsolutions.crm.auth.UsuarioAutenticadoService;
import com.aitsolutions.crm.paciente.Paciente;
import com.aitsolutions.crm.paciente.PacienteService;
import com.aitsolutions.crm.planservicio.PlanServicio;
import com.aitsolutions.crm.planservicio.PlanServicioService;
import com.aitsolutions.crm.sesion.EstadoSesion;
import com.aitsolutions.crm.sesion.SesionProgramada;
import com.aitsolutions.crm.sesion.SesionProgramadaRepository;
import com.aitsolutions.crm.tiposervicio.AutorizacionServicioService;
import com.aitsolutions.crm.tiposervicio.CapacidadServicio;
import com.aitsolutions.crm.trabajador.Trabajador;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SancionService {

    // Umbrales del apartado 4 del plan. Sin conteo de "ciclos" ni escalada a suspension
    // definitiva por ahora (decision tomada con Cristian: eso queda para una iteracion
    // posterior, junto con que pasa "despues" de una sancion, ya marcado como fuera de
    // alcance en el apartado 5 del plan).
    private static final long UMBRAL_FALTAS_ROJAS = 3;
    private static final long UMBRAL_FALTAS_NARANJAS = 6;

    private final SancionRepository sancionRepository;
    private final SesionProgramadaRepository sesionProgramadaRepository;
    private final PacienteService pacienteService;
    private final PlanServicioService planServicioService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final AutorizacionServicioService autorizacionServicioService;
    private final AuditoriaService auditoriaService;

    public SancionService(SancionRepository sancionRepository,
                           SesionProgramadaRepository sesionProgramadaRepository,
                           PacienteService pacienteService,
                           PlanServicioService planServicioService,
                           UsuarioAutenticadoService usuarioAutenticadoService,
                           AutorizacionServicioService autorizacionServicioService,
                           AuditoriaService auditoriaService) {
        this.sancionRepository = sancionRepository;
        this.sesionProgramadaRepository = sesionProgramadaRepository;
        this.pacienteService = pacienteService;
        this.planServicioService = planServicioService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.autorizacionServicioService = autorizacionServicioService;
        this.auditoriaService = auditoriaService;
    }

    public List<Sancion> listarPorPaciente(Long pacienteId) {
        Paciente paciente = pacienteService.buscarPorId(pacienteId);
        return sancionRepository.findByPacienteOrderByFechaDesc(paciente);
    }

    public List<Sancion> listarUltimas() {
        return sancionRepository.findTop20ByOrderByFechaDesc();
    }

    public Sancion crearManual(SancionRequest request) {
        Paciente paciente = pacienteService.buscarPorId(request.getPacienteId());
        PlanServicio planServicio = null;

        // Si la sancion esta ligada a un plan (ej. suspension por faltas), se comprueba la
        // capacidad fina para ESE servicio concreto. Si no lo esta (ej. tarjeta amarilla por
        // agresión, sin relacion con un plan), no hay contra qué comprobar y basta el permiso
        // general APLICAR_SANCION ya exigido por el controlador.
        if (request.getPlanServicioId() != null) {
            planServicio = planServicioService.buscarPorId(request.getPlanServicioId());
            autorizacionServicioService.verificarCapacidad(planServicio.getTipoServicio(), CapacidadServicio.APLICAR_SANCION);
        }

        Trabajador aplicadaPor = usuarioAutenticadoService.obtenerTrabajadorActual();

        Sancion sancion = new Sancion(paciente, planServicio, request.getTipo(),
                request.getMotivo(), aplicadaPor, false);
        Sancion guardada = sancionRepository.save(sancion);
        auditoriaService.registrar("SANCION_ALTA", "Sanción " + guardada.getId() + ", paciente " + paciente.getId());
        return guardada;
    }

    /**
     * Se llama desde SesionProgramadaService.marcarEstado() justo despues de marcar una
     * sesion como ROJO o NARANJA. Solo crea la sancion automatica en el momento exacto en
     * que se alcanza el umbral (conteo == umbral), para no generar una sancion duplicada
     * en cada falta siguiente una vez superado.
     */
    public void evaluarReglasAutomaticas(SesionProgramada sesion) {
        EstadoSesion estado = sesion.getEstado();
        if (estado != EstadoSesion.ROJO && estado != EstadoSesion.NARANJA) {
            return; // AMARILLO (baja medica) y VERDE nunca disparan sancion automatica
        }

        PlanServicio plan = sesion.getPlanServicio();
        long total = sesionProgramadaRepository.countByPlanServicioAndEstado(plan, estado);

        if (estado == EstadoSesion.ROJO && total == UMBRAL_FALTAS_ROJAS) {
            crearAutomatica(plan, "Alcanzadas " + UMBRAL_FALTAS_ROJAS + " faltas ROJAS en el plan de "
                    + plan.getTipoServicio().getNombre());
        } else if (estado == EstadoSesion.NARANJA && total == UMBRAL_FALTAS_NARANJAS) {
            crearAutomatica(plan, "Alcanzadas " + UMBRAL_FALTAS_NARANJAS + " faltas NARANJAS en el plan de "
                    + plan.getTipoServicio().getNombre());
        }
    }

    private void crearAutomatica(PlanServicio plan, String motivo) {
        Trabajador aplicadaPor = usuarioAutenticadoService.obtenerTrabajadorActual();
        Sancion sancion = new Sancion(plan.getPaciente(), plan, TipoSancion.SUSPENSION_TEMPORAL,
                motivo, aplicadaPor, true);
        sancionRepository.save(sancion);
        auditoriaService.registrar("SANCION_AUTOMATICA", "Plan " + plan.getId());
    }
}

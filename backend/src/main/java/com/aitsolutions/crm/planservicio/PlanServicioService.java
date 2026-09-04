package com.aitsolutions.crm.planservicio;

import com.aitsolutions.crm.auditoria.AuditoriaService;
import com.aitsolutions.crm.auth.UsuarioAutenticadoService;
import com.aitsolutions.crm.common.ResourceNotFoundException;
import com.aitsolutions.crm.paciente.Paciente;
import com.aitsolutions.crm.paciente.PacienteService;
import com.aitsolutions.crm.sesion.EstadoSesion;
import com.aitsolutions.crm.sesion.SesionProgramada;
import com.aitsolutions.crm.sesion.SesionProgramadaRepository;
import com.aitsolutions.crm.tiposervicio.AutorizacionServicioService;
import com.aitsolutions.crm.tiposervicio.CapacidadServicio;
import com.aitsolutions.crm.tiposervicio.SubServicio;
import com.aitsolutions.crm.tiposervicio.TipoServicio;
import com.aitsolutions.crm.tiposervicio.TipoServicioService;
import com.aitsolutions.crm.trabajador.Trabajador;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PlanServicioService {

    private final PlanServicioRepository planServicioRepository;
    private final SesionProgramadaRepository sesionProgramadaRepository;
    private final PacienteService pacienteService;
    private final TipoServicioService tipoServicioService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final AutorizacionServicioService autorizacionServicioService;
    private final AuditoriaService auditoriaService;

    public PlanServicioService(PlanServicioRepository planServicioRepository,
                                SesionProgramadaRepository sesionProgramadaRepository,
                                PacienteService pacienteService,
                                TipoServicioService tipoServicioService,
                                UsuarioAutenticadoService usuarioAutenticadoService,
                                AutorizacionServicioService autorizacionServicioService,
                                AuditoriaService auditoriaService) {
        this.planServicioRepository = planServicioRepository;
        this.sesionProgramadaRepository = sesionProgramadaRepository;
        this.pacienteService = pacienteService;
        this.tipoServicioService = tipoServicioService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.autorizacionServicioService = autorizacionServicioService;
        this.auditoriaService = auditoriaService;
    }

    public List<PlanServicio> buscar(Long pacienteId, Long tipoServicioId, EstadoPlanServicio estado) {
        Specification<PlanServicio> filtro = Specification
                .where(PlanServicioSpecifications.conPaciente(pacienteId))
                .and(PlanServicioSpecifications.conTipoServicio(tipoServicioId))
                .and(PlanServicioSpecifications.conEstado(estado));
        return planServicioRepository.findAll(filtro);
    }

    public PlanServicio buscarPorId(Long id) {
        return planServicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el plan de servicio con id " + id));
    }

    public List<SesionProgramada> obtenerSesiones(PlanServicio plan) {
        return sesionProgramadaRepository.findByPlanServicioOrderByFechaPrevistaAsc(plan);
    }

    @Transactional
    public PlanServicio crear(PlanServicioRequest request) {
        Paciente paciente = pacienteService.buscarPorId(request.getPacienteId());
        TipoServicio tipoServicio = tipoServicioService.buscarPorId(request.getTipoServicioId());
        autorizacionServicioService.verificarCapacidad(tipoServicio, CapacidadServicio.GESTIONAR_PLAN);
        SubServicio subServicio = resolverSubServicio(request.getSubServicioId(), tipoServicio);
        LocalDate fechaFin = resolverFechaFinObligatoria(request.getFechaInicio(),
                request.getFechaFin(), request.getDuracionSemanas());
        Trabajador creadoPor = usuarioAutenticadoService.obtenerTrabajadorActual();

        PlanServicio plan = new PlanServicio(paciente, tipoServicio, subServicio, request.getDiasSemana(),
                request.getFechaInicio(), fechaFin, creadoPor);
        plan = planServicioRepository.save(plan);

        generarCalendarioCompleto(plan);
        auditoriaService.registrar("PLAN_ALTA", "Plan " + plan.getId() + ", paciente " + paciente.getId());
        return plan;
    }

    @Transactional
    public PlanServicio actualizar(Long id, PlanServicioEdicionRequest request) {
        PlanServicio plan = buscarPorId(id);
        if (plan.getEstado() == EstadoPlanServicio.CANCELADO) {
            throw new IllegalArgumentException("No se puede editar un plan cancelado");
        }
        autorizacionServicioService.verificarCapacidad(plan.getTipoServicio(), CapacidadServicio.GESTIONAR_PLAN);

        LocalDate nuevaFechaFin = resolverFechaFinOpcional(plan.getFechaInicio(),
                request.getFechaFin(), request.getDuracionSemanas());

        plan.setDiasSemana(request.getDiasSemana());
        if (nuevaFechaFin != null) {
            plan.setFechaFin(nuevaFechaFin);
        }
        plan = planServicioRepository.save(plan);

        regenerarSesionesFuturas(plan);
        auditoriaService.registrar("PLAN_EDICION", "Plan " + plan.getId());
        return plan;
    }

    @Transactional
    public PlanServicio cambiarEstado(Long id, EstadoPlanServicio nuevoEstado) {
        PlanServicio plan = buscarPorId(id);
        autorizacionServicioService.verificarCapacidad(plan.getTipoServicio(), CapacidadServicio.GESTIONAR_PLAN);

        if (plan.getEstado() == EstadoPlanServicio.CANCELADO && nuevoEstado != EstadoPlanServicio.CANCELADO) {
            throw new IllegalArgumentException("Un plan cancelado no puede reactivarse");
        }
        plan.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoPlanServicio.FINALIZADO || nuevoEstado == EstadoPlanServicio.CANCELADO) {
            plan.setFechaFinalizacion(java.time.LocalDateTime.now());
        }
        plan = planServicioRepository.save(plan);

        // Al finalizar un plan ya no debe quedar ninguna sesion futura pendiente esperando marcado.
        if (nuevoEstado == EstadoPlanServicio.FINALIZADO) {
            eliminarSesionesFuturasPendientes(plan);
        } else if (nuevoEstado == EstadoPlanServicio.CANCELADO) {
            cancelarSesionesFuturasPendientes(plan);
        }
        auditoriaService.registrar("PLAN_ESTADO", "Plan " + plan.getId() + ": " + nuevoEstado);
        return plan;
    }

    private SubServicio resolverSubServicio(Long subServicioId, TipoServicio tipoServicio) {
        if (subServicioId == null) {
            return null;
        }
        SubServicio subServicio = tipoServicioService.buscarSubServicioPorId(subServicioId);
        if (!subServicio.getTipoServicio().getId().equals(tipoServicio.getId())) {
            throw new IllegalArgumentException(
                    "El subservicio indicado no pertenece al tipo de servicio " + tipoServicio.getNombre());
        }
        return subServicio;
    }

    /**
     * Para la creacion: fechaFin es obligatoria, se resuelve a partir de fechaFin explicita
     * o de duracionSemanas, pero exactamente una de las dos (decision tomada para no dejar
     * la API ambigua sobre cual prevalece si llegaran ambas).
     */
    private LocalDate resolverFechaFinObligatoria(LocalDate fechaInicio, LocalDate fechaFin, Integer duracionSemanas) {
        validarExclusividadFechaFin(fechaFin, duracionSemanas);
        if (fechaFin == null && duracionSemanas == null) {
            throw new IllegalArgumentException("Indica la fecha de fin del plan o su duración en semanas");
        }
        LocalDate resuelta = fechaFin != null ? fechaFin : calcularFechaFinDesdeDuracion(fechaInicio, duracionSemanas);
        validarFechaFinNoAnteriorAInicio(fechaInicio, resuelta);
        return resuelta;
    }

    /**
     * Para la edicion: si no se manda ni fechaFin ni duracionSemanas, se interpreta como
     * "no cambiar la fecha fin actual" (solo se estan editando los dias de la semana).
     */
    private LocalDate resolverFechaFinOpcional(LocalDate fechaInicio, LocalDate fechaFin, Integer duracionSemanas) {
        validarExclusividadFechaFin(fechaFin, duracionSemanas);
        if (fechaFin == null && duracionSemanas == null) {
            return null;
        }
        LocalDate resuelta = fechaFin != null ? fechaFin : calcularFechaFinDesdeDuracion(fechaInicio, duracionSemanas);
        validarFechaFinNoAnteriorAInicio(fechaInicio, resuelta);
        return resuelta;
    }

    private void validarExclusividadFechaFin(LocalDate fechaFin, Integer duracionSemanas) {
        if (fechaFin != null && duracionSemanas != null) {
            throw new IllegalArgumentException(
                    "Indica la fecha de fin o la duración en semanas, no las dos a la vez");
        }
    }

    private LocalDate calcularFechaFinDesdeDuracion(LocalDate fechaInicio, int duracionSemanas) {
        // N semanas completas contando desde fechaInicio (ej. 2 semanas desde un lunes -> termina el domingo siguiente).
        return fechaInicio.plusWeeks(duracionSemanas).minusDays(1);
    }

    private void validarFechaFinNoAnteriorAInicio(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }

    private void generarCalendarioCompleto(PlanServicio plan) {
        List<LocalDate> fechas = GeneradorCalendarioSesiones.generarFechas(
                plan.getFechaInicio(), plan.getFechaFin(), plan.getDiasSemana());
        List<SesionProgramada> sesiones = fechas.stream()
                .map(fecha -> new SesionProgramada(plan, fecha))
                .toList();
        sesionProgramadaRepository.saveAll(sesiones);
    }

    /**
     * Solo se tocan sesiones futuras que siguen PENDIENTE (nadie las ha marcado todavia).
     * Las ya registradas (VERDE/NARANJA/ROJO/AMARILLO) son historial y no se tocan,
     * sea cual sea su fecha (apartado 11 del plan).
     */
    private void regenerarSesionesFuturas(PlanServicio plan) {
        eliminarSesionesFuturasPendientes(plan);

        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.isAfter(plan.getFechaInicio()) ? hoy : plan.getFechaInicio();

        if (desde.isAfter(plan.getFechaFin())) {
            return; // el plan ya termino o termina antes de la proxima fecha posible
        }

        List<LocalDate> fechas = GeneradorCalendarioSesiones.generarFechas(desde, plan.getFechaFin(), plan.getDiasSemana());
        List<SesionProgramada> nuevas = fechas.stream()
                .filter(fecha -> sesionProgramadaRepository.findByPlanServicioAndFechaPrevista(plan, fecha).isEmpty())
                .map(fecha -> new SesionProgramada(plan, fecha))
                .toList();
        sesionProgramadaRepository.saveAll(nuevas);
    }

    private void eliminarSesionesFuturasPendientes(PlanServicio plan) {
        List<SesionProgramada> futurasPendientes = sesionProgramadaRepository
                .findByPlanServicioAndEstadoAndFechaPrevistaGreaterThanEqual(plan, EstadoSesion.PENDIENTE, LocalDate.now());
        sesionProgramadaRepository.deleteAll(futurasPendientes);
    }

    private void cancelarSesionesFuturasPendientes(PlanServicio plan) {
        List<SesionProgramada> futurasPendientes = sesionProgramadaRepository
                .findByPlanServicioAndEstadoAndFechaPrevistaGreaterThanEqual(plan, EstadoSesion.PENDIENTE, LocalDate.now());
        futurasPendientes.forEach(sesion -> sesion.setEstado(EstadoSesion.CANCELADA));
        sesionProgramadaRepository.saveAll(futurasPendientes);
    }
}

package com.aitsolutions.crm.informe;

import com.aitsolutions.crm.informe.InformeResumenResponse.PacientesKpi;
import com.aitsolutions.crm.informe.InformeResumenResponse.PlanesServicioKpi;
import com.aitsolutions.crm.informe.InformeResumenResponse.SancionesKpi;
import com.aitsolutions.crm.informe.InformeResumenResponse.SesionesKpi;
import com.aitsolutions.crm.auth.UsuarioAutenticadoService;
import com.aitsolutions.crm.common.ResourceNotFoundException;
import com.aitsolutions.crm.paciente.PacienteRepository;
import com.aitsolutions.crm.planservicio.PlanServicioRepository;
import com.aitsolutions.crm.sancion.Sancion;
import com.aitsolutions.crm.sancion.SancionRepository;
import com.aitsolutions.crm.sesion.EstadoSesion;
import com.aitsolutions.crm.sesion.SesionProgramada;
import com.aitsolutions.crm.sesion.SesionProgramadaRepository;
import com.aitsolutions.crm.informe.InformeResumenResponse.ServiciosKpi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InformeService {

    private final PacienteRepository pacienteRepository;
    private final SesionProgramadaRepository sesionProgramadaRepository;
    private final SancionRepository sancionRepository;
    private final PlanServicioRepository planServicioRepository;
    private final InformeGeneradoRepository informeGeneradoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public InformeService(PacienteRepository pacienteRepository,
                           SesionProgramadaRepository sesionProgramadaRepository,
                           SancionRepository sancionRepository,
                           PlanServicioRepository planServicioRepository,
                           InformeGeneradoRepository informeGeneradoRepository,
                           UsuarioAutenticadoService usuarioAutenticadoService) {
        this.pacienteRepository = pacienteRepository;
        this.sesionProgramadaRepository = sesionProgramadaRepository;
        this.sancionRepository = sancionRepository;
        this.planServicioRepository = planServicioRepository;
        this.informeGeneradoRepository = informeGeneradoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public InformeResumenResponse generarResumen(LocalDate desde, LocalDate hasta, String periodo, String tipoInforme) {
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }

        InformeResumenResponse resumen = new InformeResumenResponse(
                desde, hasta, periodo,
                calcularKpiPacientes(desde, hasta),
                calcularKpiSesiones(desde, hasta),
                calcularKpiSanciones(desde, hasta),
                calcularKpiPlanesServicio(desde, hasta),
                calcularKpiServicios(desde, hasta)
        );
        informeGeneradoRepository.save(new InformeGenerado(desde, hasta, periodo, tipoInforme,
                usuarioAutenticadoService.obtenerTrabajadorActual()));
        return resumen;
    }

    public List<InformeGenerado> listarHistorial() {
        return informeGeneradoRepository.findAllByOrderByFechaGeneracionDesc();
    }

    /**
     * Recalcula con las fechas originales y registra la regeneracion como un
     * nuevo elemento del historial (el registro original se conserva).
     */
    public InformeResumenResponse regenerar(Long id) {
        InformeGenerado informe = informeGeneradoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Informe no encontrado: " + id));
        return generarResumen(informe.getDesde(), informe.getHasta(), informe.getPeriodo(), informe.getTipoInforme());
    }

    private PacientesKpi calcularKpiPacientes(LocalDate desde, LocalDate hasta) {
        long activos = pacienteRepository.countByActivoTrue();
        long nuevos = pacienteRepository.countByFechaAltaBetween(desde, hasta);
        return new PacientesKpi(activos, nuevos);
    }

    private SesionesKpi calcularKpiSesiones(LocalDate desde, LocalDate hasta) {
        List<SesionProgramada> sesiones = sesionProgramadaRepository.findByFechaPrevistaBetween(desde, hasta);
        Map<EstadoSesion, Long> conteos = sesiones.stream()
                .collect(Collectors.groupingBy(SesionProgramada::getEstado, Collectors.counting()));

        long verde = conteos.getOrDefault(EstadoSesion.VERDE, 0L);
        long naranja = conteos.getOrDefault(EstadoSesion.NARANJA, 0L);
        long rojo = conteos.getOrDefault(EstadoSesion.ROJO, 0L);
        long amarillo = conteos.getOrDefault(EstadoSesion.AMARILLO, 0L);
        long pendiente = conteos.getOrDefault(EstadoSesion.PENDIENTE, 0L);
        long cancelada = conteos.getOrDefault(EstadoSesion.CANCELADA, 0L);

        // AMARILLO (baja medica) no computa para el % de asistencia, igual que no computa
        // para las reglas de sancion automatica (apartado 4 del plan); PENDIENTE tampoco,
        // porque todavia no se sabe que paso con esa sesion.
        long baseCalculo = verde + naranja + rojo;
        double porcentajeAsistencia = baseCalculo == 0 ? 0.0 : (verde * 100.0) / baseCalculo;

        return new SesionesKpi(sesiones.size(), verde, naranja, rojo, amarillo, pendiente, cancelada,
                porcentajeAsistencia);
    }

    private SancionesKpi calcularKpiSanciones(LocalDate desde, LocalDate hasta) {
        List<Sancion> sanciones = sancionRepository.findByFechaBetween(desde, hasta);
        long automaticas = sanciones.stream().filter(Sancion::isAutomatica).count();
        long manuales = sanciones.size() - automaticas;
        return new SancionesKpi(automaticas, manuales);
    }

    private PlanesServicioKpi calcularKpiPlanesServicio(LocalDate desde, LocalDate hasta) {
        // fechaCreacion/fechaFinalizacion son LocalDateTime; desde/hasta son LocalDate,
        // asi que se amplian al rango horario completo del dia (00:00:00 a 23:59:59.999...).
        LocalDateTime inicioRango = desde.atStartOfDay();
        LocalDateTime finRango = hasta.plusDays(1).atStartOfDay().minusNanos(1);

        long creados = planServicioRepository.countByFechaCreacionBetween(inicioRango, finRango);
        long finalizados = planServicioRepository.countByFechaFinalizacionBetween(inicioRango, finRango);
        return new PlanesServicioKpi(creados, finalizados);
    }

    private ServiciosKpi calcularKpiServicios(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay().minusNanos(1);
        List<com.aitsolutions.crm.planservicio.PlanServicio> planes =
                planServicioRepository.findByFechaCreacionBetween(inicio, fin);

        Map<String, Long> porServicio = planes.stream().collect(Collectors.groupingBy(
                plan -> plan.getTipoServicio().getNombre(), Collectors.counting()));
        Map<String, Long> porSexo = planes.stream().collect(Collectors.groupingBy(
                plan -> texto(plan.getPaciente().getGenero()), Collectors.counting()));
        Map<String, Long> porAsociacion = planes.stream().collect(Collectors.groupingBy(
                plan -> plan.getPaciente().getAsociacion().getNombre(), Collectors.counting()));

        List<SesionProgramada> sesiones = sesionProgramadaRepository.findByFechaPrevistaBetween(desde, hasta);
        long cancelaciones = sesiones.stream()
                .filter(sesion -> sesion.getEstado() == EstadoSesion.CANCELADA)
                .count();
        long totalSesiones = sesiones.size();
        double porcentaje = totalSesiones == 0 ? 0.0 : cancelaciones * 100.0 / totalSesiones;
        return new ServiciosKpi(planes.size(), cancelaciones, porcentaje, porServicio, porSexo, porAsociacion);
    }

    private static String texto(String valor) {
        return valor == null || valor.isBlank() ? "No informado" : valor;
    }
}

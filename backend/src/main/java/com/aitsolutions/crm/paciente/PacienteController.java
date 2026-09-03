package com.aitsolutions.crm.paciente;

import com.aitsolutions.crm.common.CambiarEstadoRequest;
import com.aitsolutions.crm.planservicio.PlanServicioResponse;
import com.aitsolutions.crm.planservicio.PlanServicioService;
import com.aitsolutions.crm.sancion.SancionResponse;
import com.aitsolutions.crm.sancion.SancionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes")
@PreAuthorize("hasAuthority('GESTIONAR_PACIENTES')")
public class PacienteController {

    private final PacienteService pacienteService;
    private final PlanServicioService planServicioService;
    private final SancionService sancionService;

    @Autowired
    public PacienteController(PacienteService pacienteService, PlanServicioService planServicioService,
                              SancionService sancionService) {
        this.pacienteService = pacienteService;
        this.planServicioService = planServicioService;
        this.sancionService = sancionService;
    }

    // GET /pacientes?asociacionId=&nombre=&page= (apartado 6 del plan).
    // El tamaño de pagina y el orden se pueden pasar tambien como parametros estandar
    // de Spring Data (size, sort), no hace falta declararlos explicitamente.
    @GetMapping
    public Page<PacienteResponse> buscar(
            @RequestParam(required = false) Long asociacionId,
            @RequestParam(required = false) String nombre,
            Pageable pageable
    ) {
        return pacienteService.buscar(asociacionId, nombre, pageable).map(PacienteResponse::new);
    }

    @GetMapping("/{id}")
    public PacienteDetalleResponse obtener(@PathVariable Long id) {
        Paciente paciente = pacienteService.buscarPorId(id);
        var planes = planServicioService.buscar(id, null, null).stream()
                .map(plan -> new PlanServicioResponse(plan, planServicioService.obtenerSesiones(plan)))
                .toList();
        var sesiones = planes.stream().flatMap(plan -> plan.getSesiones().stream()).toList();
        var sanciones = sancionService.listarPorPaciente(id).stream()
                .map(SancionResponse::new)
                .toList();
        return new PacienteDetalleResponse(paciente, planes, sesiones, sanciones);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteResponse crear(@Valid @RequestBody PacienteRequest request) {
        return new PacienteResponse(pacienteService.crear(request));
    }

    @PutMapping("/{id}")
    public PacienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
        return new PacienteResponse(pacienteService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public PacienteResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request) {
        return new PacienteResponse(pacienteService.cambiarEstado(id, request.getActivo()));
    }
}

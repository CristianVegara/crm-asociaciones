package com.aitsolutions.crm.planservicio;

import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/planes-servicio")
@PreAuthorize("hasAuthority('CREAR_PLAN_SERVICIO')")
public class PlanServicioController {

    private final PlanServicioService planServicioService;

    public PlanServicioController(PlanServicioService planServicioService) {
        this.planServicioService = planServicioService;
    }

    @GetMapping
    public List<PlanServicioResumenResponse> buscar(
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long tipoServicioId,
            @RequestParam(required = false) EstadoPlanServicio estado
    ) {
        return planServicioService.buscar(pacienteId, tipoServicioId, estado).stream()
                .map(PlanServicioResumenResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    public PlanServicioResponse obtener(@PathVariable Long id) {
        PlanServicio plan = planServicioService.buscarPorId(id);
        return new PlanServicioResponse(plan, planServicioService.obtenerSesiones(plan));
    }

    // Devuelve el plan con las sesiones ya generadas, para que el cliente pueda
    // mostrar el calendario resultante inmediatamente tras el alta.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanServicioResponse crear(@Valid @RequestBody PlanServicioRequest request) {
        PlanServicio plan = planServicioService.crear(request);
        return new PlanServicioResponse(plan, planServicioService.obtenerSesiones(plan));
    }

    @PutMapping("/{id}")
    public PlanServicioResponse actualizar(@PathVariable Long id, @Valid @RequestBody PlanServicioEdicionRequest request) {
        PlanServicio plan = planServicioService.actualizar(id, request);
        return new PlanServicioResponse(plan, planServicioService.obtenerSesiones(plan));
    }

    @PatchMapping("/{id}/estado")
    public PlanServicioResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoPlanRequest request) {
        PlanServicio plan = planServicioService.cambiarEstado(id, request.getEstado());
        return new PlanServicioResponse(plan, planServicioService.obtenerSesiones(plan));
    }
}

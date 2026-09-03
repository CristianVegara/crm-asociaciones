package com.aitsolutions.crm.sesion;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sesiones")
public class SesionProgramadaController {

    private final SesionProgramadaService sesionProgramadaService;

    public SesionProgramadaController(SesionProgramadaService sesionProgramadaService) {
        this.sesionProgramadaService = sesionProgramadaService;
    }

    // Vista de agenda para recepcion (apartado 6 del plan).
    @GetMapping
    @PreAuthorize("hasAuthority('REGISTRAR_ASISTENCIA')")
    public List<SesionAgendaResponse> buscar(
            @RequestParam(required = false) Long planServicioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) EstadoSesion estado
    ) {
        return sesionProgramadaService.buscar(planServicioId, desde, hasta, estado).stream()
                .map(SesionAgendaResponse::new)
                .toList();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('REGISTRAR_ASISTENCIA')")
    public SesionAgendaResponse marcarEstado(@PathVariable Long id, @Valid @RequestBody MarcarAsistenciaRequest request) {
        return new SesionAgendaResponse(sesionProgramadaService.marcarEstado(id, request.getEstado()));
    }

    // Mismo permiso que gestiona el plan al que pertenece la sesion (CREAR_PLAN_SERVICIO):
    // borrar una sesion suelta es gestión del plan, no registro de asistencia.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('CREAR_PLAN_SERVICIO')")
    public void eliminar(@PathVariable Long id) {
        sesionProgramadaService.eliminar(id);
    }
}

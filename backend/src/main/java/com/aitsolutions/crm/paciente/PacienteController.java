package com.aitsolutions.crm.paciente;

import com.aitsolutions.crm.common.CambiarEstadoRequest;
import jakarta.validation.Valid;
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

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
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
    public PacienteResponse obtener(@PathVariable Long id) {
        return new PacienteResponse(pacienteService.buscarPorId(id));
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

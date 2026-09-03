package com.aitsolutions.crm.asociacion;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Sin permiso propio en el catálogo (apartado 12 del plan): comparte GESTIONAR_PACIENTES
// porque la asociación es el contenedor del paciente.
@RestController
@RequestMapping("/asociaciones")
@PreAuthorize("hasAuthority('GESTIONAR_PACIENTES')")
public class AsociacionController {

    private final AsociacionService asociacionService;

    public AsociacionController(AsociacionService asociacionService) {
        this.asociacionService = asociacionService;
    }

    @GetMapping
    public List<AsociacionResponse> listar() {
        return asociacionService.listarTodas().stream().map(AsociacionResponse::new).toList();
    }

    @GetMapping("/{id}")
    public AsociacionResponse obtener(@PathVariable Long id) {
        return new AsociacionResponse(asociacionService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AsociacionResponse crear(@Valid @RequestBody AsociacionRequest request) {
        return new AsociacionResponse(asociacionService.crear(request));
    }

    @PutMapping("/{id}")
    public AsociacionResponse actualizar(@PathVariable Long id, @Valid @RequestBody AsociacionRequest request) {
        return new AsociacionResponse(asociacionService.actualizar(id, request));
    }
}

package com.aitsolutions.crm.rol;

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

// Gestión de roles y sus permisos: reservado a quien administra la estructura de la asociación.
@RestController
@RequestMapping("/roles")
@PreAuthorize("hasAuthority('GESTIONAR_TRABAJADORES')")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public List<RolResponse> listar() {
        return rolService.listarTodos().stream().map(RolResponse::new).toList();
    }

    @GetMapping("/{id}")
    public RolResponse obtener(@PathVariable Long id) {
        return new RolResponse(rolService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolResponse crear(@Valid @RequestBody RolRequest request) {
        return new RolResponse(rolService.crear(request));
    }

    @PutMapping("/{id}")
    public RolResponse actualizar(@PathVariable Long id, @Valid @RequestBody RolRequest request) {
        return new RolResponse(rolService.actualizar(id, request));
    }

    @PutMapping("/{id}/permisos")
    public RolResponse asignarPermisos(@PathVariable Long id, @Valid @RequestBody AsignarPermisosRequest request) {
        return new RolResponse(rolService.asignarPermisos(id, request));
    }
}

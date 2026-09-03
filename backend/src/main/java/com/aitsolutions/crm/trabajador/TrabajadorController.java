package com.aitsolutions.crm.trabajador;

import com.aitsolutions.crm.common.CambiarEstadoRequest;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trabajadores")
@PreAuthorize("hasAuthority('GESTIONAR_TRABAJADORES')")
public class TrabajadorController {

    private final TrabajadorService trabajadorService;

    public TrabajadorController(TrabajadorService trabajadorService) {
        this.trabajadorService = trabajadorService;
    }

    @GetMapping
    public List<TrabajadorResponse> listar() {
        return trabajadorService.listarTodos().stream().map(TrabajadorResponse::new).toList();
    }

    @GetMapping("/{id}")
    public TrabajadorResponse obtener(@PathVariable Long id) {
        return new TrabajadorResponse(trabajadorService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrabajadorResponse crear(@Valid @RequestBody TrabajadorRequest request) {
        return new TrabajadorResponse(trabajadorService.crear(request));
    }

    @PutMapping("/{id}")
    public TrabajadorResponse actualizar(@PathVariable Long id, @Valid @RequestBody TrabajadorRequest request) {
        return new TrabajadorResponse(trabajadorService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public TrabajadorResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request) {
        return new TrabajadorResponse(trabajadorService.cambiarEstado(id, request.getActivo()));
    }
}

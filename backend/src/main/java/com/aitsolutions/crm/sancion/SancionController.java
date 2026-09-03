package com.aitsolutions.crm.sancion;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sanciones")
@PreAuthorize("hasAuthority('APLICAR_SANCION')")
public class SancionController {

    private final SancionService sancionService;

    public SancionController(SancionService sancionService) {
        this.sancionService = sancionService;
    }

    @GetMapping
    public List<SancionResponse> listar(@RequestParam Long pacienteId) {
        return sancionService.listarPorPaciente(pacienteId).stream().map(SancionResponse::new).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SancionResponse crear(@Valid @RequestBody SancionRequest request) {
        return new SancionResponse(sancionService.crearManual(request));
    }
}

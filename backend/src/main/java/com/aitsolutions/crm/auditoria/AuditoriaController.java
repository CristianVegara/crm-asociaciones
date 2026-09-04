package com.aitsolutions.crm.auditoria;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auditoria")
@PreAuthorize("hasAuthority('GESTIONAR_TRABAJADORES')")
public class AuditoriaController {
    private final AuditoriaEventoRepository repository;

    public AuditoriaController(AuditoriaEventoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AuditoriaEventoResponse> listar(@RequestParam(defaultValue = "100") int limite) {
        int limiteSeguro = Math.max(1, Math.min(limite, 500));
        return repository.findAllByOrderByFechaDesc(PageRequest.of(0, limiteSeguro))
                .stream().map(AuditoriaEventoResponse::new).toList();
    }
}

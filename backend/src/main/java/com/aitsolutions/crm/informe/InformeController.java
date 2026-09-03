package com.aitsolutions.crm.informe;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/informes")
@PreAuthorize("hasAuthority('VER_INFORMES')")
public class InformeController {

    private final InformeService informeService;

    public InformeController(InformeService informeService) {
        this.informeService = informeService;
    }

    @GetMapping("/resumen")
    public InformeResumenResponse resumen(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false, defaultValue = "general") String tipoInforme
    ) {
        return informeService.generarResumen(desde, hasta, periodo, tipoInforme);
    }

    @GetMapping("/historial")
    public List<InformeHistorialResponse> historial() {
        return informeService.listarHistorial().stream().map(InformeHistorialResponse::new).toList();
    }

    @PostMapping("/{id}/regenerar")
    public InformeResumenResponse regenerar(@PathVariable Long id) {
        return informeService.regenerar(id);
    }
}

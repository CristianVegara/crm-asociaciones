package com.aitsolutions.crm.tiposervicio;

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

@RestController
@PreAuthorize("hasAuthority('GESTIONAR_CATALOGO_SERVICIOS')")
public class TipoServicioController {

    private final TipoServicioService tipoServicioService;

    public TipoServicioController(TipoServicioService tipoServicioService) {
        this.tipoServicioService = tipoServicioService;
    }

    @GetMapping("/tipos-servicio")
    @PreAuthorize("hasAnyAuthority('GESTIONAR_CATALOGO_SERVICIOS', 'CREAR_PLAN_SERVICIO')")
    public List<TipoServicioResponse> listar() {
        return tipoServicioService.listarTodos().stream()
                .map(tipo -> new TipoServicioResponse(tipo, tipoServicioService.obtenerResponsables(tipo)))
                .toList();
    }

    @PostMapping("/tipos-servicio")
    @ResponseStatus(HttpStatus.CREATED)
    public TipoServicioResponse crear(@Valid @RequestBody TipoServicioRequest request) {
        TipoServicio tipoServicio = tipoServicioService.crear(request);
        return new TipoServicioResponse(tipoServicio, List.of());
    }

    @PutMapping("/tipos-servicio/{id}")
    public TipoServicioResponse actualizar(@PathVariable Long id, @Valid @RequestBody TipoServicioRequest request) {
        TipoServicio tipoServicio = tipoServicioService.actualizar(id, request);
        return new TipoServicioResponse(tipoServicio, tipoServicioService.obtenerResponsables(tipoServicio));
    }

    @PostMapping("/tipos-servicio/{id}/subservicios")
    @ResponseStatus(HttpStatus.CREATED)
    public TipoServicioResponse agregarSubServicio(@PathVariable Long id, @Valid @RequestBody SubServicioRequest request) {
        TipoServicio tipoServicio = tipoServicioService.agregarSubServicio(id, request);
        return new TipoServicioResponse(tipoServicio, tipoServicioService.obtenerResponsables(tipoServicio));
    }

    @PutMapping("/subservicios/{id}")
    public SubServicioResponse actualizarSubServicio(@PathVariable Long id, @Valid @RequestBody SubServicioRequest request) {
        return new SubServicioResponse(tipoServicioService.actualizarSubServicio(id, request));
    }

    @PutMapping("/tipos-servicio/{id}/responsables")
    public TipoServicioResponse asignarResponsables(@PathVariable Long id, @Valid @RequestBody AsignarResponsablesRequest request) {
        tipoServicioService.asignarResponsables(id, request.getResponsables());
        TipoServicio tipoServicio = tipoServicioService.buscarPorId(id);
        return new TipoServicioResponse(tipoServicio, tipoServicioService.obtenerResponsables(tipoServicio));
    }
}

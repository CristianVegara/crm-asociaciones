package com.aitsolutions.crm.permiso;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Catalogo fijo de permisos (GET /permisos, solo lectura: ver apartado 6 del plan).
 */
@RestController
@RequestMapping("/permisos")
public class PermisoController {

    @GetMapping
    public List<PermisoResponse> listar() {
        return Arrays.stream(Permiso.values())
                .map(PermisoResponse::new)
                .toList();
    }
}

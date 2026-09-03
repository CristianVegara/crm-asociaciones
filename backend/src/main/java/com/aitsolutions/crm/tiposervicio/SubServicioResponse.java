package com.aitsolutions.crm.tiposervicio;

public class SubServicioResponse {

    private final Long id;
    private final String nombre;
    private final boolean activo;

    public SubServicioResponse(SubServicio subServicio) {
        this.id = subServicio.getId();
        this.nombre = subServicio.getNombre();
        this.activo = subServicio.isActivo();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActivo() {
        return activo;
    }
}

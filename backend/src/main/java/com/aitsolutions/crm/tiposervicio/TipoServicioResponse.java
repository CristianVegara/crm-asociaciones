package com.aitsolutions.crm.tiposervicio;

import java.util.List;

public class TipoServicioResponse {

    private final Long id;
    private final String nombre;
    private final String icono;
    private final String color;
    private final boolean activo;
    private final List<SubServicioResponse> subServicios;
    private final List<ResponsableResponse> responsables;

    public TipoServicioResponse(TipoServicio tipoServicio, List<TipoServicioResponsable> responsables) {
        this.id = tipoServicio.getId();
        this.nombre = tipoServicio.getNombre();
        this.icono = tipoServicio.getIcono();
        this.color = tipoServicio.getColor();
        this.activo = tipoServicio.isActivo();
        this.subServicios = tipoServicio.getSubServicios().stream().map(SubServicioResponse::new).toList();
        this.responsables = responsables.stream().map(ResponsableResponse::new).toList();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIcono() {
        return icono;
    }

    public String getColor() {
        return color;
    }

    public boolean isActivo() {
        return activo;
    }

    public List<SubServicioResponse> getSubServicios() {
        return subServicios;
    }

    public List<ResponsableResponse> getResponsables() {
        return responsables;
    }
}

package com.aitsolutions.crm.tiposervicio;

public class ResponsableResponse {

    private final Long rolId;
    private final String rolNombre;
    private final CapacidadServicio capacidad;

    public ResponsableResponse(TipoServicioResponsable responsable) {
        this.rolId = responsable.getRol().getId();
        this.rolNombre = responsable.getRol().getNombre();
        this.capacidad = responsable.getCapacidad();
    }

    public Long getRolId() {
        return rolId;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public CapacidadServicio getCapacidad() {
        return capacidad;
    }
}

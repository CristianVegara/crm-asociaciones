package com.aitsolutions.crm.asociacion;

public class AsociacionResponse {

    private final Long id;
    private final String nombre;
    private final String direccion;
    private final String contacto;

    public AsociacionResponse(Asociacion asociacion) {
        this.id = asociacion.getId();
        this.nombre = asociacion.getNombre();
        this.direccion = asociacion.getDireccion();
        this.contacto = asociacion.getContacto();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getContacto() {
        return contacto;
    }
}

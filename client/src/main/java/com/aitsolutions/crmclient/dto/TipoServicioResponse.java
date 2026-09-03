package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TipoServicioResponse {

    private Long id;
    private String nombre;
    private boolean activo;
    private List<ResponsableResponse> responsables = new ArrayList<>();
    private List<SubServicioResponse> subServicios = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<ResponsableResponse> getResponsables() {
        return responsables;
    }

    public void setResponsables(List<ResponsableResponse> responsables) {
        this.responsables = responsables;
    }

    public List<SubServicioResponse> getSubServicios() {
        return subServicios;
    }

    public void setSubServicios(List<SubServicioResponse> subServicios) {
        this.subServicios = subServicios;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

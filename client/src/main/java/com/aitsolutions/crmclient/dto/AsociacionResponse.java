package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AsociacionResponse {
    private Long id;
    private String nombre;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    @Override public String toString() { return nombre; }
}

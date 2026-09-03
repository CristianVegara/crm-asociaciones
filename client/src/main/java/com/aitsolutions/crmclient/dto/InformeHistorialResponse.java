package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InformeHistorialResponse {
    private Long id;
    private String desde;
    private String hasta;
    private String periodo;
    private String fechaGeneracion;
    private String generadoPorNombre;

    public Long getId() { return id; }
    public String getDesde() { return desde; }
    public String getHasta() { return hasta; }
    public String getPeriodo() { return periodo; }
    public String getFechaGeneracion() { return fechaGeneracion; }
    public String getGeneradoPorNombre() { return generadoPorNombre; }
}

package com.aitsolutions.crm.informe;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InformeHistorialResponse {

    private final Long id;
    private final LocalDate desde;
    private final LocalDate hasta;
    private final String periodo;
    private final String tipoInforme;
    private final LocalDateTime fechaGeneracion;
    private final Long generadoPorId;
    private final String generadoPorNombre;

    public InformeHistorialResponse(InformeGenerado informe) {
        this.id = informe.getId();
        this.desde = informe.getDesde();
        this.hasta = informe.getHasta();
        this.periodo = informe.getPeriodo();
        this.tipoInforme = informe.getTipoInforme();
        this.fechaGeneracion = informe.getFechaGeneracion();
        this.generadoPorId = informe.getGeneradoPor().getId();
        this.generadoPorNombre = informe.getGeneradoPor().getNombre() + " "
                + informe.getGeneradoPor().getApellidos();
    }

    public Long getId() { return id; }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public String getPeriodo() { return periodo; }
    public String getTipoInforme() { return tipoInforme; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public Long getGeneradoPorId() { return generadoPorId; }
    public String getGeneradoPorNombre() { return generadoPorNombre; }
}

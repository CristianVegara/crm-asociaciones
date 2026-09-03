package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InformeResumenResponse {

    private String desde;
    private String hasta;
    private String periodo;
    private PacientesKpi pacientes;
    private SesionesKpi sesiones;
    private SancionesKpi sanciones;
    private PlanesServicioKpi planesServicio;
    private ServiciosKpi servicios;

    public String getDesde() {
        return desde;
    }

    public void setDesde(String desde) {
        this.desde = desde;
    }

    public String getHasta() {
        return hasta;
    }

    public void setHasta(String hasta) {
        this.hasta = hasta;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public PacientesKpi getPacientes() {
        return pacientes;
    }

    public void setPacientes(PacientesKpi pacientes) {
        this.pacientes = pacientes;
    }

    public SesionesKpi getSesiones() {
        return sesiones;
    }

    public void setSesiones(SesionesKpi sesiones) {
        this.sesiones = sesiones;
    }

    public SancionesKpi getSanciones() {
        return sanciones;
    }

    public void setSanciones(SancionesKpi sanciones) {
        this.sanciones = sanciones;
    }

    public PlanesServicioKpi getPlanesServicio() {
        return planesServicio;
    }

    public void setPlanesServicio(PlanesServicioKpi planesServicio) {
        this.planesServicio = planesServicio;
    }

    public ServiciosKpi getServicios() { return servicios; }
    public void setServicios(ServiciosKpi servicios) { this.servicios = servicios; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PacientesKpi {
        private long activos;
        private long nuevosEnPeriodo;

        public long getActivos() {
            return activos;
        }

        public void setActivos(long activos) {
            this.activos = activos;
        }

        public long getNuevosEnPeriodo() {
            return nuevosEnPeriodo;
        }

        public void setNuevosEnPeriodo(long nuevosEnPeriodo) {
            this.nuevosEnPeriodo = nuevosEnPeriodo;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SesionesKpi {
        private long total;
        private long verde;
        private long naranja;
        private long rojo;
        private long amarillo;
        private long pendiente;
        private double porcentajeAsistencia;
        private long cancelada;

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getVerde() {
            return verde;
        }

        public void setVerde(long verde) {
            this.verde = verde;
        }

        public long getNaranja() {
            return naranja;
        }

        public void setNaranja(long naranja) {
            this.naranja = naranja;
        }

        public long getRojo() {
            return rojo;
        }

        public void setRojo(long rojo) {
            this.rojo = rojo;
        }

        public long getAmarillo() {
            return amarillo;
        }

        public void setAmarillo(long amarillo) {
            this.amarillo = amarillo;
        }

        public long getPendiente() {
            return pendiente;
        }

        public void setPendiente(long pendiente) {
            this.pendiente = pendiente;
        }

        public double getPorcentajeAsistencia() {
            return porcentajeAsistencia;
        }

        public void setPorcentajeAsistencia(double porcentajeAsistencia) {
            this.porcentajeAsistencia = porcentajeAsistencia;
        }

        public long getCancelada() { return cancelada; }
        public void setCancelada(long cancelada) { this.cancelada = cancelada; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SancionesKpi {
        private long automaticas;
        private long manuales;
        private long total;

        public long getAutomaticas() {
            return automaticas;
        }

        public void setAutomaticas(long automaticas) {
            this.automaticas = automaticas;
        }

        public long getManuales() {
            return manuales;
        }

        public void setManuales(long manuales) {
            this.manuales = manuales;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlanesServicioKpi {
        private long creadosEnPeriodo;
        private long finalizadosEnPeriodo;

        public long getCreadosEnPeriodo() {
            return creadosEnPeriodo;
        }

        public void setCreadosEnPeriodo(long creadosEnPeriodo) {
            this.creadosEnPeriodo = creadosEnPeriodo;
        }

        public long getFinalizadosEnPeriodo() {
            return finalizadosEnPeriodo;
        }

        public void setFinalizadosEnPeriodo(long finalizadosEnPeriodo) {
            this.finalizadosEnPeriodo = finalizadosEnPeriodo;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServiciosKpi {
        private long total;
        private long cancelaciones;
        private double porcentajeCancelaciones;
        private Map<String, Long> porServicio;
        private Map<String, Long> porSexo;
        private Map<String, Long> porAsociacion;

        public long getTotal() { return total; }
        public long getCancelaciones() { return cancelaciones; }
        public double getPorcentajeCancelaciones() { return porcentajeCancelaciones; }
        public Map<String, Long> getPorServicio() { return porServicio; }
        public Map<String, Long> getPorSexo() { return porSexo; }
        public Map<String, Long> getPorAsociacion() { return porAsociacion; }
    }
}

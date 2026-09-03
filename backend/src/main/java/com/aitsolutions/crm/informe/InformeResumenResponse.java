package com.aitsolutions.crm.informe;

import java.time.LocalDate;
import java.util.Map;

/**
 * "periodo" es puramente informativo (una etiqueta que el cliente manda para mostrarla en
 * la UI, ej. "trimestral"): el calculo real siempre se hace sobre desde/hasta, decision
 * tomada con Cristian para no tener que interpretar "mes actual" vs "mes natural" etc.
 */
public class InformeResumenResponse {

    private final LocalDate desde;
    private final LocalDate hasta;
    private final String periodo;
    private final PacientesKpi pacientes;
    private final SesionesKpi sesiones;
    private final SancionesKpi sanciones;
    private final PlanesServicioKpi planesServicio;
    private final ServiciosKpi servicios;

    public InformeResumenResponse(LocalDate desde, LocalDate hasta, String periodo,
                                   PacientesKpi pacientes, SesionesKpi sesiones,
                                   SancionesKpi sanciones, PlanesServicioKpi planesServicio, ServiciosKpi servicios) {
        this.desde = desde;
        this.hasta = hasta;
        this.periodo = periodo;
        this.pacientes = pacientes;
        this.sesiones = sesiones;
        this.sanciones = sanciones;
        this.planesServicio = planesServicio;
        this.servicios = servicios;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public String getPeriodo() {
        return periodo;
    }

    public PacientesKpi getPacientes() {
        return pacientes;
    }

    public SesionesKpi getSesiones() {
        return sesiones;
    }

    public SancionesKpi getSanciones() {
        return sanciones;
    }

    public PlanesServicioKpi getPlanesServicio() {
        return planesServicio;
    }

    public ServiciosKpi getServicios() {
        return servicios;
    }

    public static class PacientesKpi {
        private final long activos;
        private final long nuevosEnPeriodo;

        public PacientesKpi(long activos, long nuevosEnPeriodo) {
            this.activos = activos;
            this.nuevosEnPeriodo = nuevosEnPeriodo;
        }

        public long getActivos() {
            return activos;
        }

        public long getNuevosEnPeriodo() {
            return nuevosEnPeriodo;
        }
    }

    public static class SesionesKpi {
        private final long total;
        private final long verde;
        private final long naranja;
        private final long rojo;
        private final long amarillo;
        private final long pendiente;
        private final long cancelada;
        private final double porcentajeAsistencia;

        public SesionesKpi(long total, long verde, long naranja, long rojo, long amarillo,
                            long pendiente, long cancelada, double porcentajeAsistencia) {
            this.total = total;
            this.verde = verde;
            this.naranja = naranja;
            this.rojo = rojo;
            this.amarillo = amarillo;
            this.pendiente = pendiente;
            this.cancelada = cancelada;
            this.porcentajeAsistencia = porcentajeAsistencia;
        }

        public long getTotal() {
            return total;
        }

        public long getVerde() {
            return verde;
        }

        public long getNaranja() {
            return naranja;
        }

        public long getRojo() {
            return rojo;
        }

        public long getAmarillo() {
            return amarillo;
        }

        public long getPendiente() {
            return pendiente;
        }

        public double getPorcentajeAsistencia() {
            return porcentajeAsistencia;
        }

        public long getCancelada() {
            return cancelada;
        }
    }

    public static class SancionesKpi {
        private final long automaticas;
        private final long manuales;
        private final long total;

        public SancionesKpi(long automaticas, long manuales) {
            this.automaticas = automaticas;
            this.manuales = manuales;
            this.total = automaticas + manuales;
        }

        public long getAutomaticas() {
            return automaticas;
        }

        public long getManuales() {
            return manuales;
        }

        public long getTotal() {
            return total;
        }
    }

    public static class PlanesServicioKpi {
        private final long creadosEnPeriodo;
        private final long finalizadosEnPeriodo;

        public PlanesServicioKpi(long creadosEnPeriodo, long finalizadosEnPeriodo) {
            this.creadosEnPeriodo = creadosEnPeriodo;
            this.finalizadosEnPeriodo = finalizadosEnPeriodo;
        }

        public long getCreadosEnPeriodo() {
            return creadosEnPeriodo;
        }

        public long getFinalizadosEnPeriodo() {
            return finalizadosEnPeriodo;
        }
    }

    public static class ServiciosKpi {
            private final long total;
            private final long cancelaciones;
            private final double porcentajeCancelaciones;
            private final Map<String, Long> porServicio;
            private final Map<String, Long> porSexo;
            private final Map<String, Long> porAsociacion;

            public ServiciosKpi(long total, long cancelaciones, double porcentajeCancelaciones,
                                Map<String, Long> porServicio, Map<String, Long> porSexo,
                                Map<String, Long> porAsociacion) {
                this.total = total;
                this.cancelaciones = cancelaciones;
                this.porcentajeCancelaciones = porcentajeCancelaciones;
                this.porServicio = porServicio;
                this.porSexo = porSexo;
                this.porAsociacion = porAsociacion;
            }

            public long getTotal() { return total; }
            public long getCancelaciones() { return cancelaciones; }
            public double getPorcentajeCancelaciones() { return porcentajeCancelaciones; }
            public Map<String, Long> getPorServicio() { return porServicio; }
            public Map<String, Long> getPorSexo() { return porSexo; }
            public Map<String, Long> getPorAsociacion() { return porAsociacion; }
    }
}

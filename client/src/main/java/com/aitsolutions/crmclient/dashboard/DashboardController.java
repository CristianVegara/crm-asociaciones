package com.aitsolutions.crmclient.dashboard;

import com.aitsolutions.crmclient.dto.InformeResumenResponse;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.LocalDate;

public class DashboardController {
    @FXML private Label pacientesActivos, sesionesTotal, asistencia, sancionesTotal, planesCreados, etiquetaEstado;

    @FXML
    private void initialize() {
        cargar();
    }

    @FXML private void onActualizarClick() { cargar(); }

    private void cargar() {
        etiquetaEstado.setText("Cargando...");
        LocalDate hasta = LocalDate.now();
        LocalDate desde = hasta.withDayOfYear(1);
        Task<InformeResumenResponse> tarea = new Task<>() {
            @Override protected InformeResumenResponse call() {
                return ApiClient.getInstance().get("/informes/resumen?desde=" + desde + "&hasta=" + hasta,
                        InformeResumenResponse.class);
            }
        };
        tarea.setOnSucceeded(e -> {
            InformeResumenResponse r = tarea.getValue();
            pacientesActivos.setText(String.valueOf(r.getPacientes().getActivos()));
            sesionesTotal.setText(String.valueOf(r.getSesiones().getTotal()));
            asistencia.setText(String.format("%.1f%%", r.getSesiones().getPorcentajeAsistencia()));
            sancionesTotal.setText(String.valueOf(r.getSanciones().getTotal()));
            planesCreados.setText(String.valueOf(r.getPlanesServicio().getCreadosEnPeriodo()));
            etiquetaEstado.setText("Datos del " + r.getDesde() + " al " + r.getHasta());
        });
        tarea.setOnFailed(e -> etiquetaEstado.setText(tarea.getException() instanceof ApiException
                ? tarea.getException().getMessage() : "No se pudieron cargar los indicadores"));
        Thread hilo = new Thread(tarea, "dashboard-api");
        hilo.setDaemon(true);
        hilo.start();
    }
}

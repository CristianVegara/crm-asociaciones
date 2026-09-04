package com.aitsolutions.crmclient.auditoria;

import com.aitsolutions.crmclient.dto.AuditoriaEventoResponse;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;

public class AuditoriaController {
    @FXML private TableView<AuditoriaEventoResponse> tabla;
    @FXML private TableColumn<AuditoriaEventoResponse, String> columnaFecha, columnaUsuario,
            columnaMetodo, columnaRuta, columnaEstado, columnaIp, columnaAccion, columnaDetalle;
    @FXML private TextField campoFiltro;
    @FXML private Label etiquetaEstado;

    @FXML
    private void initialize() {
        columnaFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha()));
        columnaUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsuario()));
        columnaMetodo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMetodo()));
        columnaRuta.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRuta()));
        columnaEstado.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getEstadoHttp())));
        columnaIp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDireccionIp()));
        columnaAccion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAccion()));
        columnaDetalle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDetalle()));
        cargar();
    }

    @FXML private void onActualizarClick() { cargar(); }

    private void cargar() {
        etiquetaEstado.setText("Cargando auditoría...");
        Task<List<AuditoriaEventoResponse>> tarea = new Task<>() {
            @Override protected List<AuditoriaEventoResponse> call() {
                return ApiClient.getInstance().getConTipoGenerico("/auditoria?limite=500",
                        new TypeReference<List<AuditoriaEventoResponse>>() {});
            }
        };
        tarea.setOnSucceeded(e -> {
            String filtro = campoFiltro.getText() == null ? "" : campoFiltro.getText().trim().toLowerCase();
            List<AuditoriaEventoResponse> filtrados = tarea.getValue().stream()
                    .filter(evento -> filtro.isBlank()
                            || evento.getUsuario().toLowerCase().contains(filtro)
                            || evento.getRuta().toLowerCase().contains(filtro)
                            || evento.getMetodo().toLowerCase().contains(filtro))
                    .toList();
            tabla.setItems(FXCollections.observableArrayList(filtrados));
            etiquetaEstado.setText(filtrados.size() + " evento(s)");
        });
        tarea.setOnFailed(e -> etiquetaEstado.setText(tarea.getException() instanceof ApiException
                ? tarea.getException().getMessage() : "No se pudo cargar la auditoría"));
        Thread hilo = new Thread(tarea, "auditoria-api");
        hilo.setDaemon(true);
        hilo.start();
    }
}

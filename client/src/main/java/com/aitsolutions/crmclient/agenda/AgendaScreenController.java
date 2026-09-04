package com.aitsolutions.crmclient.agenda;

import com.aitsolutions.crmclient.dto.MarcarAsistenciaRequest;
import com.aitsolutions.crmclient.dto.SesionAgendaResponse;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.util.List;

public class AgendaScreenController {
    @FXML private DatePicker campoDesde;
    @FXML private DatePicker campoHasta;
    @FXML private ComboBox<String> comboEstado;
    @FXML private TableView<SesionAgendaResponse> tablaSesiones;
    @FXML private TableColumn<SesionAgendaResponse, String> columnaFecha;
    @FXML private TableColumn<SesionAgendaResponse, String> columnaPaciente;
    @FXML private TableColumn<SesionAgendaResponse, String> columnaServicio;
    @FXML private TableColumn<SesionAgendaResponse, String> columnaEstado;
    @FXML private Label etiquetaEstado;

    @FXML
    private void initialize() {
        campoDesde.setValue(LocalDate.now());
        campoHasta.setValue(LocalDate.now().plusDays(7));
        comboEstado.setItems(FXCollections.observableArrayList(
                "Todos", "PENDIENTE", "VERDE", "NARANJA", "ROJO", "AMARILLO", "CANCELADA"));
        comboEstado.setValue("Todos");
        configurarColumnas();
        cargarSesiones();
    }

    private void configurarColumnas() {
        columnaFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaPrevista()));
        columnaPaciente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPacienteNombreCompleto()));
        columnaServicio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoServicioNombre()));
        columnaEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        columnaEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                setText(empty ? null : estado);
                getStyleClass().removeIf(s -> s.startsWith("estado-"));
                if (!empty && estado != null) getStyleClass().add("estado-" + estado.toLowerCase());
            }
        });
        tablaSesiones.setRowFactory(table -> {
            TableRow<SesionAgendaResponse> fila = new TableRow<>();
            MenuItem marcar = new MenuItem("Marcar estado...");
            marcar.setOnAction(e -> mostrarMarcado(fila.getItem()));
            ContextMenu menu = new ContextMenu(marcar);
            fila.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(fila.emptyProperty())
                            .then((ContextMenu) null).otherwise(menu));
            return fila;
        });
    }

    @FXML private void onBuscarClick() { cargarSesiones(); }

    private void cargarSesiones() {
        if (campoDesde.getValue() != null && campoHasta.getValue() != null
                && campoDesde.getValue().isAfter(campoHasta.getValue())) {
            etiquetaEstado.setText("La fecha inicial no puede ser posterior a la final");
            return;
        }
        StringBuilder ruta = new StringBuilder("/sesiones?");
        if (campoDesde.getValue() != null) ruta.append("desde=").append(campoDesde.getValue()).append('&');
        if (campoHasta.getValue() != null) ruta.append("hasta=").append(campoHasta.getValue()).append('&');
        if (comboEstado.getValue() != null && !"Todos".equals(comboEstado.getValue())) {
            ruta.append("estado=").append(comboEstado.getValue());
        }
        etiquetaEstado.setText("Cargando...");
        ejecutar(() -> ApiClient.getInstance().getConTipoGenerico(ruta.toString(),
                new TypeReference<List<SesionAgendaResponse>>() {}), sesiones -> {
            tablaSesiones.setItems(FXCollections.observableArrayList(sesiones));
            etiquetaEstado.setText(sesiones.size() + " sesión(es)");
        });
    }

    private void mostrarMarcado(SesionAgendaResponse sesion) {
        if (sesion == null || "CANCELADA".equals(sesion.getEstado())) return;
        ChoiceDialog<String> dialogo = new ChoiceDialog<>("VERDE",
                "VERDE", "NARANJA", "ROJO", "AMARILLO");
        dialogo.setTitle("Registrar asistencia");
        dialogo.setHeaderText("Sesión de " + sesion.getPacienteNombreCompleto());
        dialogo.setContentText("Resultado:");
        dialogo.showAndWait().ifPresent(estado -> ejecutar(() ->
                ApiClient.getInstance().patch("/sesiones/" + sesion.getId(),
                        new MarcarAsistenciaRequest(estado), SesionAgendaResponse.class),
                actualizada -> {
                    etiquetaEstado.setText("Sesión actualizada");
                    cargarSesiones();
                }));
    }

    private <T> void ejecutar(java.util.concurrent.Callable<T> llamada,
                              java.util.function.Consumer<T> exito) {
        Task<T> tarea = new Task<>() {
            @Override protected T call() throws Exception { return llamada.call(); }
        };
        tarea.setOnSucceeded(e -> exito.accept(tarea.getValue()));
        tarea.setOnFailed(e -> {
            Throwable error = tarea.getException();
            etiquetaEstado.setText(error instanceof ApiException
                    ? error.getMessage() : "No se pudo completar la operación");
        });
        Thread hilo = new Thread(tarea, "agenda-api");
        hilo.setDaemon(true);
        hilo.start();
    }
}

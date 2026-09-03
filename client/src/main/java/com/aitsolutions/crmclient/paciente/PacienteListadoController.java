package com.aitsolutions.crmclient.paciente;

import com.aitsolutions.crmclient.dto.PacienteResponse;
import com.aitsolutions.crmclient.dto.PaginaRespuesta;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PacienteListadoController {

    @FXML
    private TextField campoBusqueda;

    @FXML
    private TableView<PacienteResponse> tablaPacientes;

    @FXML
    private TableColumn<PacienteResponse, String> columnaNombre;

    @FXML
    private TableColumn<PacienteResponse, String> columnaApellidos;

    @FXML
    private TableColumn<PacienteResponse, String> columnaExpediente;

    @FXML
    private TableColumn<PacienteResponse, String> columnaAsociacion;

    @FXML
    private TableColumn<PacienteResponse, String> columnaActivo;

    @FXML
    private Label etiquetaEstado;

    @FXML
    private void initialize() {
        configurarColumnas();
        cargarPacientes(null);
    }

    private void configurarColumnas() {
        // Los DTO son POJOs planos (no propiedades JavaFX), asi que se envuelve cada valor
        // en un SimpleStringProperty en vez de usar PropertyValueFactory.
        columnaNombre.setCellValueFactory(datos -> new SimpleStringProperty(datos.getValue().getNombre()));
        columnaApellidos.setCellValueFactory(datos -> new SimpleStringProperty(datos.getValue().getApellidos()));
        columnaExpediente.setCellValueFactory(datos -> new SimpleStringProperty(datos.getValue().getNumeroExpediente()));
        columnaAsociacion.setCellValueFactory(datos -> new SimpleStringProperty(datos.getValue().getAsociacionNombre()));
        columnaActivo.setCellValueFactory(datos -> new SimpleStringProperty(datos.getValue().isActivo() ? "Sí" : "No"));
    }

    @FXML
    private void onBuscarClick() {
        cargarPacientes(campoBusqueda.getText());
    }

    private void cargarPacientes(String filtroNombre) {
        etiquetaEstado.setText("Cargando...");

        Task<PaginaRespuesta<PacienteResponse>> tarea = new Task<>() {
            @Override
            protected PaginaRespuesta<PacienteResponse> call() {
                String ruta = "/pacientes";
                if (filtroNombre != null && !filtroNombre.isBlank()) {
                    ruta += "?nombre=" + URLEncoder.encode(filtroNombre, StandardCharsets.UTF_8);
                }
                return ApiClient.getInstance().getConTipoGenerico(
                        ruta, new TypeReference<PaginaRespuesta<PacienteResponse>>() {
                        });
            }
        };

        tarea.setOnSucceeded(evento -> {
            ObservableList<PacienteResponse> filas = FXCollections.observableArrayList(tarea.getValue().getContent());
            tablaPacientes.setItems(filas);
            etiquetaEstado.setText(tarea.getValue().getTotalElements() + " paciente(s)");
        });

        tarea.setOnFailed(evento -> {
            Throwable causa = tarea.getException();
            String mensaje = (causa instanceof ApiException apiException)
                    ? apiException.getMessage()
                    : "No se pudo cargar el listado de pacientes.";
            etiquetaEstado.setText(mensaje);
        });

        new Thread(tarea).start();
    }
}

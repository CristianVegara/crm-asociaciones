package com.aitsolutions.crmclient.sancion;

import com.aitsolutions.crmclient.dto.SancionRequest;
import com.aitsolutions.crmclient.dto.SancionResponse;
import com.aitsolutions.crmclient.dto.TipoSancion;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class SancionScreenController {

    @FXML
    private TextField campoPacienteId;

    @FXML
    private Label etiquetaEstado;

    @FXML
    private TableView<SancionResponse> tablaSanciones;

    @FXML
    private TableColumn<SancionResponse, String> columnaFecha;

    @FXML
    private TableColumn<SancionResponse, String> columnaTipo;

    @FXML
    private TableColumn<SancionResponse, String> columnaMotivo;

    @FXML
    private TableColumn<SancionResponse, String> columnaAplicadaPor;

    @FXML
    private TableColumn<SancionResponse, String> columnaAutomatica;

    @FXML
    private ComboBox<TipoSancion> comboTipoSancion;

    @FXML
    private TextField campoPlanServicioId;

    @FXML
    private TextField campoMotivo;

    @FXML
    private void initialize() {
        columnaFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha()));
        columnaTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().toString()));
        columnaMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivo()));
        columnaAplicadaPor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAplicadaPorNombre()));
        columnaAutomatica.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isAutomatica() ? "Sí" : "No"));

        comboTipoSancion.setItems(FXCollections.observableArrayList(TipoSancion.values()));
    }

    @FXML
    private void onBuscarClick() {
        Long pacienteId = leerLong(campoPacienteId.getText());
        if (pacienteId == null) {
            etiquetaEstado.setText("Introduce un id de paciente válido");
            return;
        }

        ejecutarAsync(
                () -> ApiClient.getInstance().getConTipoGenerico(
                        "/sanciones?pacienteId=" + pacienteId, new TypeReference<List<SancionResponse>>() {
                        }),
                lista -> {
                    tablaSanciones.setItems(FXCollections.observableArrayList(lista));
                    etiquetaEstado.setText(lista.size() + " sanción(es)");
                }
        );
    }

    @FXML
    private void onAplicarClick() {
        Long pacienteId = leerLong(campoPacienteId.getText());
        TipoSancion tipo = comboTipoSancion.getValue();

        if (pacienteId == null || tipo == null || campoMotivo.getText().isBlank()) {
            etiquetaEstado.setText("Indica el id de paciente, el tipo y el motivo");
            return;
        }

        // Campo opcional: solo se manda si el usuario ha escrito un id de plan valido.
        Long planServicioId = leerLong(campoPlanServicioId.getText());

        SancionRequest request = new SancionRequest(pacienteId, planServicioId, tipo, campoMotivo.getText());

        ejecutarAsync(
                () -> ApiClient.getInstance().post("/sanciones", request, SancionResponse.class, true),
                creada -> {
                    etiquetaEstado.setText("Sanción aplicada");
                    campoMotivo.clear();
                    campoPlanServicioId.clear();
                    comboTipoSancion.setValue(null);
                    onBuscarClick(); // refresca la tabla con la nueva sanción incluida
                }
        );
    }

    private Long leerLong(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private <T> void ejecutarAsync(Callable<T> llamada, Consumer<T> alExito) {
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() throws Exception {
                return llamada.call();
            }
        };
        tarea.setOnSucceeded(evento -> alExito.accept(tarea.getValue()));
        tarea.setOnFailed(evento -> {
            Throwable causa = tarea.getException();
            String mensaje = (causa instanceof ApiException apiException)
                    ? apiException.getMessage()
                    : "Error de conexión";
            etiquetaEstado.setText(mensaje);
        });
        new Thread(tarea).start();
    }
}

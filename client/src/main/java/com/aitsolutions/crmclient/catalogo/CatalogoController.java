package com.aitsolutions.crmclient.catalogo;

import com.aitsolutions.crmclient.dto.*;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class CatalogoController {
    @FXML private ComboBox<TipoServicioResponse> comboTipo;
    @FXML private ListView<SubServicioResponse> listaSubservicios;
    @FXML private TextField campoTipo, campoSubservicio;
    @FXML private Label etiquetaEstado;

    @FXML private void initialize() {
        comboTipo.valueProperty().addListener((o, a, b) -> actualizarSubservicios());
        cargarTipos();
    }

    @FXML private void onCrearTipoClick() {
        if (campoTipo.getText().isBlank()) { etiquetaEstado.setText("Indica el nombre del servicio"); return; }
        ejecutar(() -> ApiClient.getInstance().post("/tipos-servicio",
                new TipoServicioRequest(campoTipo.getText().trim()), TipoServicioResponse.class, true),
                r -> { campoTipo.clear(); etiquetaEstado.setText("Servicio creado"); cargarTipos(); });
    }

    @FXML private void onCrearSubservicioClick() {
        TipoServicioResponse tipo = comboTipo.getValue();
        if (tipo == null || campoSubservicio.getText().isBlank()) {
            etiquetaEstado.setText("Selecciona un servicio e indica el subservicio"); return;
        }

        ejecutar(() -> ApiClient.getInstance().post("/tipos-servicio/" + tipo.getId() + "/subservicios",
                new SubServicioRequest(campoSubservicio.getText().trim()), TipoServicioResponse.class, true),
                r -> { campoSubservicio.clear(); etiquetaEstado.setText("Subservicio creado"); cargarTipos(); });
    }

    @FXML private void onEditarTipoClick() {
        TipoServicioResponse tipo = comboTipo.getValue();
        if (tipo == null) {
            etiquetaEstado.setText("Selecciona un tipo de servicio");
            return;
        }
        TextInputDialog dialogo = new TextInputDialog(tipo.getNombre());
        dialogo.setTitle("Editar tipo de servicio");
        dialogo.setHeaderText("Renombrar servicio");
        dialogo.setContentText("Nombre:");
        dialogo.showAndWait().map(String::trim).filter(nombre -> !nombre.isBlank())
                .ifPresent(nombre -> ejecutar(() -> ApiClient.getInstance().put(
                        "/tipos-servicio/" + tipo.getId(), new TipoServicioRequest(nombre),
                        TipoServicioResponse.class), respuesta -> {
                    etiquetaEstado.setText("Servicio actualizado");
                    cargarTipos();
                }));
    }

    @FXML private void onEditarSubservicioClick() {
        SubServicioResponse subservicio = listaSubservicios.getSelectionModel().getSelectedItem();
        TipoServicioResponse tipo = comboTipo.getValue();
        if (tipo == null || subservicio == null) {
            etiquetaEstado.setText("Selecciona un subservicio");
            return;
        }
        TextInputDialog dialogo = new TextInputDialog(subservicio.getNombre());
        dialogo.setTitle("Editar subservicio");
        dialogo.setHeaderText("Renombrar subservicio");
        dialogo.setContentText("Nombre:");
        dialogo.showAndWait().map(String::trim).filter(nombre -> !nombre.isBlank())
                .ifPresent(nombre -> ejecutar(() -> ApiClient.getInstance().put(
                        "/subservicios/" + subservicio.getId(), new SubServicioRequest(nombre),
                        SubServicioResponse.class), respuesta -> {
                    etiquetaEstado.setText("Subservicio actualizado");
                    cargarTipos();
                }));
    }

    private void cargarTipos() {
        ejecutar(() -> ApiClient.getInstance().getConTipoGenerico("/tipos-servicio",
                new TypeReference<List<TipoServicioResponse>>() {}),
                lista -> {
                    Long id = comboTipo.getValue() == null ? null : comboTipo.getValue().getId();
                    comboTipo.setItems(FXCollections.observableArrayList(lista));
                    if (id != null) lista.stream().filter(t -> t.getId().equals(id)).findFirst()
                            .ifPresent(t -> comboTipo.getSelectionModel().select(t));
                    else if (!lista.isEmpty()) comboTipo.getSelectionModel().selectFirst();
                    actualizarSubservicios();
                });
    }

    private void actualizarSubservicios() {
        TipoServicioResponse tipo = comboTipo.getValue();
        listaSubservicios.setItems(FXCollections.observableArrayList(
                tipo == null ? List.of() : tipo.getSubServicios()));
    }

    private <T> void ejecutar(java.util.concurrent.Callable<T> llamada, java.util.function.Consumer<T> exito) {
        Task<T> tarea = new Task<>() { @Override protected T call() throws Exception { return llamada.call(); } };
        tarea.setOnSucceeded(e -> exito.accept(tarea.getValue()));
        tarea.setOnFailed(e -> etiquetaEstado.setText(tarea.getException() instanceof ApiException
                ? tarea.getException().getMessage() : "No se pudo completar la operación"));
        Thread hilo = new Thread(tarea, "catalogo-api"); hilo.setDaemon(true); hilo.start();
    }
}

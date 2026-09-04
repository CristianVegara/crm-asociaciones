package com.aitsolutions.crmclient.asociacion;

import com.aitsolutions.crmclient.dto.*;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.List;
import java.util.Locale;

public class AsociacionController {
    @FXML private TableView<AsociacionResponse> tabla;
    @FXML private TableColumn<AsociacionResponse, String> columnaNombre, columnaDireccion, columnaContacto;
    @FXML private Label etiquetaEstado;
    @FXML private TextField campoBusqueda;
    private FilteredList<AsociacionResponse> asociaciones;

    @FXML private void initialize() {
        columnaNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        columnaDireccion.setCellValueFactory(c -> new SimpleStringProperty(valor(c.getValue().getDireccion())));
        columnaContacto.setCellValueFactory(c -> new SimpleStringProperty(valor(c.getValue().getContacto())));
        campoBusqueda.textProperty().addListener((obs, anterior, actual) -> filtrar(actual));
        cargar();
    }

    @FXML private void onNuevaClick() { mostrarFormulario(null); }
    @FXML private void onEditarClick() { mostrarFormulario(tabla.getSelectionModel().getSelectedItem()); }
    @FXML private void onActualizarClick() { cargar(); }
    @FXML private void onBuscarClick() { filtrar(campoBusqueda.getText()); }

    private void mostrarFormulario(AsociacionResponse existente) {
        Dialog<AsociacionRequest> dialogo = new Dialog<>();
        dialogo.setTitle(existente == null ? "Nueva asociación" : "Editar asociación");
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);
        TextField nombre = new TextField(valor(existente == null ? null : existente.getNombre()));
        TextField direccion = new TextField(valor(existente == null ? null : existente.getDireccion()));
        TextField contacto = new TextField(valor(existente == null ? null : existente.getContacto()));
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(12));
        grid.addRow(0, new Label("Nombre *"), nombre);
        grid.addRow(1, new Label("Dirección"), direccion);
        grid.addRow(2, new Label("Contacto"), contacto);
        dialogo.getDialogPane().setContent(grid);
        dialogo.setResultConverter(b -> b == guardar && !nombre.getText().isBlank()
                ? new AsociacionRequest(nombre.getText().trim(), direccion.getText().trim(), contacto.getText().trim()) : null);
        dialogo.showAndWait().ifPresent(request -> ejecutar(() -> existente == null
                ? ApiClient.getInstance().post("/asociaciones", request, AsociacionResponse.class, true)
                : ApiClient.getInstance().put("/asociaciones/" + existente.getId(), request, AsociacionResponse.class),
                r -> { etiquetaEstado.setText("Asociación guardada"); cargar(); }));
    }

    private String valor(String valor) { return valor == null ? "" : valor; }

    private void cargar() {
        etiquetaEstado.setText("Cargando...");
        ejecutar(() -> ApiClient.getInstance().getConTipoGenerico("/asociaciones",
                new TypeReference<List<AsociacionResponse>>() {}), lista -> {
            asociaciones = new FilteredList<>(FXCollections.observableArrayList(lista));
            tabla.setItems(asociaciones);
            filtrar(campoBusqueda.getText());
        });
    }

    private void filtrar(String texto) {
        if (asociaciones == null) {
            return;
        }
        String criterio = valor(texto).trim().toLowerCase(Locale.ROOT);
        asociaciones.setPredicate(asociacion -> criterio.isBlank()
                || contiene(asociacion.getNombre(), criterio)
                || contiene(asociacion.getDireccion(), criterio)
                || contiene(asociacion.getContacto(), criterio));
        etiquetaEstado.setText(asociaciones.size() + " asociación(es)");
    }

    private boolean contiene(String valor, String criterio) {
        return valor(valor).toLowerCase(Locale.ROOT).contains(criterio);
    }

    private <T> void ejecutar(java.util.concurrent.Callable<T> llamada, java.util.function.Consumer<T> exito) {
        Task<T> tarea = new Task<>() { @Override protected T call() throws Exception { return llamada.call(); } };
        tarea.setOnSucceeded(e -> exito.accept(tarea.getValue()));
        tarea.setOnFailed(e -> etiquetaEstado.setText(tarea.getException() instanceof ApiException
                ? tarea.getException().getMessage() : "No se pudo completar la operación"));
        Thread hilo = new Thread(tarea, "asociaciones-api"); hilo.setDaemon(true); hilo.start();
    }
}

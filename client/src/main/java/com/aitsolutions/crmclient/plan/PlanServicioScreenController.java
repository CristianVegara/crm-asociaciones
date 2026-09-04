package com.aitsolutions.crmclient.plan;

import com.aitsolutions.crmclient.dto.*;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

public class PlanServicioScreenController {
    @FXML private ComboBox<PacienteResponse> comboPaciente;
    @FXML private ComboBox<TipoServicioResponse> comboServicio;
    @FXML private ComboBox<String> comboEstado;
    @FXML private TableView<PlanServicioResumen> tablaPlanes;
    @FXML private Label etiquetaEstado;

    @FXML private void initialize() {
        comboEstado.setItems(FXCollections.observableArrayList("Todos", "ACTIVO", "FINALIZADO", "CANCELADO"));
        comboEstado.setValue("Todos");
        cargarCatalogos();
        comboPaciente.valueProperty().addListener((o, a, b) -> cargarPlanes());
        comboServicio.valueProperty().addListener((o, a, b) -> cargarPlanes());
        comboEstado.valueProperty().addListener((o, a, b) -> cargarPlanes());
        configurarTabla();
    }

    private void configurarTabla() {
        tablaPlanes.getColumns().addAll(columna("Paciente", p -> p.paciente()), columna("Servicio", p -> p.servicio()),
                columna("Inicio", p -> p.inicio()), columna("Fin", p -> p.fin()), columna("Estado", p -> p.estado()));
        tablaPlanes.setRowFactory(t -> {
            TableRow<PlanServicioResumen> fila = new TableRow<>();
            MenuItem cancelar = new MenuItem("Cancelar plan");
            cancelar.setOnAction(e -> cancelarPlan(fila.getItem()));
            ContextMenu menu = new ContextMenu(cancelar);
            fila.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(fila.emptyProperty())
                            .then((ContextMenu) null).otherwise(menu));
            return fila;
        });
    }

    private void cancelarPlan(PlanServicioResumen plan) {
        if (plan == null || "CANCELADO".equals(plan.estado())) return;
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Cancelar el plan seleccionado?", ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setHeaderText(null);
        confirmacion.showAndWait().filter(ButtonType.OK::equals).ifPresent(ok ->
                ejecutar(() -> ApiClient.getInstance().patch("/planes-servicio/" + plan.id() + "/estado",
                        new CambiarEstadoPlanRequest("CANCELADO"), PlanServicioResponse.class), r -> {
                    etiquetaEstado.setText("Plan cancelado");
                    cargarPlanes();
                }));
    }

    private TableColumn<PlanServicioResumen, String> columna(String titulo, java.util.function.Function<PlanServicioResumen, String> valor) {
        TableColumn<PlanServicioResumen, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(valor.apply(c.getValue())));
        return columna;
    }

    @FXML private void onNuevoPlanClick() { mostrarFormulario(); }

    private void cargarCatalogos() {
        ejecutar(() -> ApiClient.getInstance().getConTipoGenerico("/pacientes?size=1000",
                new TypeReference<PaginaRespuesta<PacienteResponse>>() {}), r -> comboPaciente.setItems(FXCollections.observableArrayList(r.getContent())));
        ejecutar(() -> ApiClient.getInstance().getConTipoGenerico("/tipos-servicio",
                new TypeReference<java.util.List<TipoServicioResponse>>() {}), r -> comboServicio.setItems(FXCollections.observableArrayList(r)));
        cargarPlanes();
    }

    private void cargarPlanes() {
        if (tablaPlanes == null) return;
        StringBuilder ruta = new StringBuilder("/planes-servicio?");
        if (comboPaciente.getValue() != null) ruta.append("pacienteId=").append(comboPaciente.getValue().getId()).append('&');
        if (comboServicio.getValue() != null) ruta.append("tipoServicioId=").append(comboServicio.getValue().getId()).append('&');
        if (!"Todos".equals(comboEstado.getValue()) && comboEstado.getValue() != null) ruta.append("estado=").append(comboEstado.getValue());
        ejecutar(() -> ApiClient.getInstance().getConTipoGenerico(ruta.toString(),
                new TypeReference<java.util.List<PlanServicioResumen>>() {}), r -> tablaPlanes.setItems(FXCollections.observableArrayList(r)));
    }

    private void mostrarFormulario() {
        Dialog<PlanServicioRequest> dialogo = new Dialog<>();
        dialogo.setTitle("Nuevo plan de servicio");
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);
        DatePicker inicio = new DatePicker(LocalDate.now()), fin = new DatePicker();
        ComboBox<PacienteResponse> paciente = new ComboBox<>(comboPaciente.getItems());
        ComboBox<TipoServicioResponse> servicio = new ComboBox<>(comboServicio.getItems());
        paciente.setPromptText("Seleccione un paciente");
        servicio.setPromptText("Seleccione un servicio");
        TextField semanas = new TextField(); semanas.setPromptText("Duración en semanas");
        CheckBox[] checks = new CheckBox[7]; HBox dias = new HBox(6);
        for (DayOfWeek dia : DayOfWeek.values()) { CheckBox c = new CheckBox(dia.name().substring(0, 3)); c.setUserData(dia); checks[dia.getValue()-1] = c; dias.getChildren().add(c); }
        GridPane grid = new GridPane(); grid.setHgap(8); grid.setVgap(8); grid.setPadding(new Insets(12));
        grid.addRow(0, new Label("Paciente"), paciente);
        grid.addRow(1, new Label("Servicio"), servicio);
        grid.addRow(2, new Label("Inicio"), inicio);
        grid.addRow(3, new Label("Fin"), fin);
        grid.addRow(4, new Label("Duración"), semanas);
        grid.addRow(5, new Label("Días"), dias);
        dialogo.getDialogPane().setContent(grid);
        dialogo.setResultConverter(b -> {
            if (b != guardar || paciente.getValue() == null || servicio.getValue() == null) return null;
            Set<DayOfWeek> seleccionados = EnumSet.noneOf(DayOfWeek.class);
            for (CheckBox c : checks) if (c.isSelected()) seleccionados.add((DayOfWeek) c.getUserData());
            Integer duracion = semanas.getText().isBlank() ? null : Integer.valueOf(semanas.getText().trim());
            return new PlanServicioRequest(paciente.getValue().getId(), servicio.getValue().getId(), null, seleccionados, inicio.getValue(), fin.getValue(), duracion);
        });
        dialogo.showAndWait().ifPresent(request -> ejecutar(() -> ApiClient.getInstance().post("/planes-servicio", request, PlanServicioResponse.class, true), r -> { etiquetaEstado.setText("Plan creado"); cargarPlanes(); }));
    }

    private <T> void ejecutar(java.util.concurrent.Callable<T> llamada, java.util.function.Consumer<T> exito) {
        Task<T> tarea = new Task<>() { protected T call() throws Exception { return llamada.call(); } };
        tarea.setOnSucceeded(e -> exito.accept(tarea.getValue()));
        tarea.setOnFailed(e -> etiquetaEstado.setText(tarea.getException() instanceof ApiException a ? a.getMessage() : "No se pudo completar la operación"));
        new Thread(tarea).start();
    }

    public record PlanServicioResumen(Long id, Long pacienteId, String pacienteNombreCompleto, String tipoServicioNombre,
                                      String subServicioNombre, String fechaInicio, String fechaFin, String estado) {
        String paciente() { return pacienteNombreCompleto; } String servicio() { return tipoServicioNombre; }
        String inicio() { return fechaInicio; } String fin() { return fechaFin; }
    }
}

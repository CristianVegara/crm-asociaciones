package com.aitsolutions.crmclient.paciente;

import com.aitsolutions.crmclient.dto.CambiarEstadoPlanRequest;
import com.aitsolutions.crmclient.dto.PacienteDetalleResponse;
import com.aitsolutions.crmclient.dto.PlanServicioEdicionRequest;
import com.aitsolutions.crmclient.dto.PlanServicioRequest;
import com.aitsolutions.crmclient.dto.PlanServicioResponse;
import com.aitsolutions.crmclient.dto.SancionResponse;
import com.aitsolutions.crmclient.dto.SesionProgramadaResponse;
import com.aitsolutions.crmclient.dto.SubServicioResponse;
import com.aitsolutions.crmclient.dto.TipoServicioResponse;
import com.aitsolutions.crmclient.dto.MarcarAsistenciaRequest;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Ficha modal del paciente. Las operaciones se ejecutan contra los mismos
 * endpoints que la agenda y el modulo de planes, manteniendo el listado ligero.
 */
public final class PacienteFichaDialog {

    private final Long pacienteId;
    private final Dialog<Void> dialogo = new Dialog<>();
    private final Label estado = new Label();
    private final Label datos = new Label();
    private final TableView<PlanServicioResponse> tablaPlanes = new TableView<>();
    private final TableView<SesionProgramadaResponse> tablaSesiones = new TableView<>();
    private final TableView<SancionResponse> tablaSanciones = new TableView<>();
    private final ComboBox<String> comboEstadoSesion = new ComboBox<>();
    private final ComboBox<String> comboFiltroEstadoSesion = new ComboBox<>();
    private final FilteredList<SesionProgramadaResponse> sesionesFiltradas =
            new FilteredList<>(FXCollections.observableArrayList());
    private final Button borrarSesion = new Button("Borrar sesión pendiente");
    private final Button marcarSesion = new Button("Marcar asistencia");
    private final Map<Long, String> nombrePlanPorId = new HashMap<>();

    private PacienteFichaDialog(Long pacienteId, String titulo) {
        this.pacienteId = pacienteId;
        dialogo.setTitle("Ficha del paciente");
        dialogo.setHeaderText(titulo);
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialogo.getDialogPane().setContent(crearContenido());
        dialogo.setResizable(true);
        tablaPlanes.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nuevo) -> actualizarBotonesPlan(nuevo));
        tablaSesiones.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nuevo) -> actualizarBotonesSesion(nuevo));
        cargarDetalle();
    }

    public static void mostrar(Long pacienteId, String titulo, Window propietario) {
        PacienteFichaDialog ficha = new PacienteFichaDialog(pacienteId, titulo);
        if (propietario != null) {
            ficha.dialogo.initOwner(propietario);
        }
        ficha.dialogo.showAndWait();
    }

    private Node crearContenido() {
        configurarTablas();
        comboEstadoSesion.setItems(FXCollections.observableArrayList("VERDE", "NARANJA", "ROJO", "AMARILLO"));
        comboEstadoSesion.setPromptText("Nuevo estado");
        comboFiltroEstadoSesion.setItems(FXCollections.observableArrayList(
                "Todos", "PENDIENTE", "VERDE", "NARANJA", "ROJO", "AMARILLO", "CANCELADA"));
        comboFiltroEstadoSesion.setValue("Todos");
        comboFiltroEstadoSesion.setPromptText("Filtrar estado");
        comboFiltroEstadoSesion.valueProperty().addListener((obs, anterior, nuevo) ->
                sesionesFiltradas.setPredicate(sesion -> "Todos".equals(nuevo)
                        || nuevo.equals(sesion.getEstado())));
        borrarSesion.setDisable(true);
        marcarSesion.setDisable(true);
        borrarSesion.setOnAction(e -> borrarSesionSeleccionada());
        marcarSesion.setOnAction(e -> marcarSesionSeleccionada());

        Button nuevoPlan = new Button("Nuevo plan");
        Button editarPlan = new Button("Editar plan");
        Button cancelarPlan = new Button("Cancelar plan");
        nuevoPlan.setOnAction(e -> mostrarFormularioPlan(null));
        editarPlan.setOnAction(e -> {
            PlanServicioResponse plan = tablaPlanes.getSelectionModel().getSelectedItem();
            if (plan != null) mostrarFormularioPlan(plan);
        });
        cancelarPlan.setOnAction(e -> cancelarPlanSeleccionado());
        HBox accionesPlan = new HBox(8, nuevoPlan, editarPlan, cancelarPlan);
        VBox planes = new VBox(8, accionesPlan, tablaPlanes);
        VBox sesiones = new VBox(8,
                new HBox(8, comboFiltroEstadoSesion, comboEstadoSesion, marcarSesion, borrarSesion),
                tablaSesiones);
        VBox sanciones = new VBox(tablaSanciones);
        TabPane pestañas = new TabPane(
                new Tab("Planes", planes),
                new Tab("Sesiones", sesiones),
                new Tab("Sanciones", sanciones));
        pestañas.getTabs().forEach(tab -> tab.setClosable(false));

        VBox contenido = new VBox(10, datos, pestañas, estado);
        contenido.setPadding(new Insets(12));
        VBox.setVgrow(pestañas, javafx.scene.layout.Priority.ALWAYS);
        return contenido;
    }

    private void configurarTablas() {
        TableColumn<PlanServicioResponse, String> plan = new TableColumn<>("Servicio");
        plan.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                texto(d.getValue().getTipoServicioNombre())
                        + (d.getValue().getSubServicioNombre() == null ? "" : " / " + d.getValue().getSubServicioNombre())));
        TableColumn<PlanServicioResponse, String> periodo = new TableColumn<>("Periodo");
        periodo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                texto(d.getValue().getFechaInicio()) + " → " + texto(d.getValue().getFechaFin())));
        TableColumn<PlanServicioResponse, String> dias = new TableColumn<>("Días");
        dias.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getDiasSemana() == null ? "" : d.getValue().getDiasSemana().stream()
                        .map(PacienteFichaDialog::nombreDiaCompleto)
                        .collect(java.util.stream.Collectors.joining(", "))));
        TableColumn<PlanServicioResponse, String> estadoPlan = new TableColumn<>("Estado");
        estadoPlan.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEstado()));
        tablaPlanes.getColumns().addAll(plan, periodo, dias, estadoPlan);
        tablaPlanes.setPrefHeight(180);

        TableColumn<SesionProgramadaResponse, String> sesionFecha = new TableColumn<>("Fecha prevista");
        sesionFecha.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getFechaPrevista()));
        TableColumn<SesionProgramadaResponse, String> sesionPlan = new TableColumn<>("Plan");
        sesionPlan.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                nombrePlanPorId.getOrDefault(d.getValue().getPlanServicioId(), "Plan " + d.getValue().getPlanServicioId())));
        TableColumn<SesionProgramadaResponse, String> sesionEstado = new TableColumn<>("Estado");
        sesionEstado.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEstado()));
        sesionEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String valor, boolean vacio) {
                super.updateItem(valor, vacio);
                setText(null);
                setGraphic(vacio || valor == null ? null : crearEtiquetaEstado(valor));
            }
        });
        tablaSesiones.getColumns().addAll(sesionFecha, sesionPlan, sesionEstado);
        tablaSesiones.setPrefHeight(220);

        TableColumn<SancionResponse, String> sancionFecha = new TableColumn<>("Fecha");
        sancionFecha.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getFecha())));
        TableColumn<SancionResponse, String> sancionTipo = new TableColumn<>("Tipo");
        sancionTipo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getTipo())));
        sancionTipo.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String valor, boolean vacio) {
                super.updateItem(valor, vacio);
                setText(null);
                setGraphic(vacio || valor == null ? null : crearEtiquetaSancion(valor));
            }
        });
        TableColumn<SancionResponse, String> sancionMotivo = new TableColumn<>("Motivo");
        sancionMotivo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMotivo()));
        tablaSanciones.getColumns().addAll(sancionFecha, sancionTipo, sancionMotivo);
        tablaSanciones.setPrefHeight(180);
    }

    private void cargarDetalle() {
        ejecutarAsync(() -> ApiClient.getInstance().get("/pacientes/" + pacienteId, PacienteDetalleResponse.class),
                this::mostrarDetalle);
    }

    private void mostrarDetalle(PacienteDetalleResponse detalle) {
        datos.setText(detalle.getNombre() + " " + detalle.getApellidos()
                + "  ·  Expediente: " + texto(detalle.getNumeroExpediente())
                + "  ·  Nacimiento: " + texto(detalle.getFechaNacimiento())
                + "  ·  Género: " + texto(detalle.getGenero())
                + "  ·  DNI: " + texto(detalle.getDni())
                + "  ·  Teléfono: " + texto(detalle.getTelefono())
                + "  ·  Email: " + texto(detalle.getEmail())
                + "  ·  Asociación: " + texto(detalle.getAsociacionNombre())
                + "  ·  Alta: " + texto(detalle.getFechaAlta())
                + "  ·  " + (detalle.isActivo() ? "Activo" : "Inactivo"));
        nombrePlanPorId.clear();
        detalle.getPlanes().forEach(p -> nombrePlanPorId.put(p.getId(), texto(p.getTipoServicioNombre())));
        tablaPlanes.setItems(FXCollections.observableArrayList(detalle.getPlanes()));
        List<SesionProgramadaResponse> sesiones = new ArrayList<>();
        detalle.getPlanes().forEach(p -> sesiones.addAll(p.getSesiones()));
        sesionesFiltradas.setAll(sesiones);
        tablaSesiones.setItems(sesionesFiltradas);
        tablaSanciones.setItems(FXCollections.observableArrayList(detalle.getSanciones()));
        estado.setText("Ficha actualizada");
        actualizarBotonesPlan(tablaPlanes.getSelectionModel().getSelectedItem());
        actualizarBotonesSesion(tablaSesiones.getSelectionModel().getSelectedItem());
    }

    private void actualizarBotonesPlan(PlanServicioResponse plan) {
        // The action buttons are intentionally enabled only for active plans.
        // The dialog contains no reference to the buttons created in the local
        // scope, so the checks in each action remain the authoritative guard.
    }

    private void actualizarBotonesSesion(SesionProgramadaResponse sesion) {
        boolean pendiente = sesion != null && "PENDIENTE".equals(sesion.getEstado());
        borrarSesion.setDisable(!pendiente);
        marcarSesion.setDisable(sesion == null || !"PENDIENTE".equals(sesion.getEstado()));
    }

    private void cancelarPlanSeleccionado() {
        PlanServicioResponse plan = tablaPlanes.getSelectionModel().getSelectedItem();
        if (plan == null || !"ACTIVO".equals(plan.getEstado())) {
            estado.setText("Selecciona un plan activo");
            return;
        }
        ejecutarAsync(() -> ApiClient.getInstance().patch("/planes-servicio/" + plan.getId() + "/estado",
                        new CambiarEstadoPlanRequest("CANCELADO"), PlanServicioResponse.class),
                actualizado -> {
                    estado.setText("Plan cancelado; se conserva su histórico");
                    cargarDetalle();
                });
    }

    private void borrarSesionSeleccionada() {
        SesionProgramadaResponse sesion = tablaSesiones.getSelectionModel().getSelectedItem();
        if (sesion == null || !"PENDIENTE".equals(sesion.getEstado())) {
            estado.setText("Solo se pueden borrar sesiones pendientes");
            return;
        }
        ejecutarAsync(() -> {
            ApiClient.getInstance().delete("/sesiones/" + sesion.getId());
            return Boolean.TRUE;
        }, ignorado -> {
            estado.setText("Sesión eliminada");
            cargarDetalle();
        });
    }

    private void marcarSesionSeleccionada() {
        SesionProgramadaResponse sesion = tablaSesiones.getSelectionModel().getSelectedItem();
        String nuevoEstado = comboEstadoSesion.getValue();
        if (sesion == null || nuevoEstado == null || !"PENDIENTE".equals(sesion.getEstado())) {
            estado.setText("Selecciona una sesión pendiente y un estado");
            return;
        }
        ejecutarAsync(() -> ApiClient.getInstance().patch("/sesiones/" + sesion.getId(),
                        new MarcarAsistenciaRequest(nuevoEstado), SesionProgramadaResponse.class),
                ignorado -> {
                    estado.setText("Asistencia registrada");
                    comboEstadoSesion.setValue(null);
                    cargarDetalle();
                });
    }

    private void mostrarFormularioPlan(PlanServicioResponse planExistente) {
        boolean editar = planExistente != null;
        Dialog<Object> formulario = new Dialog<>();
        formulario.setTitle(editar ? "Editar plan de servicio" : "Nuevo plan de servicio");
        formulario.setHeaderText(editar ? "Edita días y duración del plan" : "Datos del plan para " + pacienteId);
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        formulario.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        ComboBox<TipoServicioResponse> tipos = new ComboBox<>();
        ComboBox<SubServicioResponse> subservicios = new ComboBox<>();
        DatePicker inicio = new DatePicker();
        DatePicker fin = new DatePicker();
        TextField duracion = new TextField();
        duracion.setPromptText("semanas");
        inicio.setDisable(editar);
        tipos.setDisable(editar);
        subservicios.setDisable(editar);
        if (editar) {
            inicio.setValue(parseDate(planExistente.getFechaInicio()));
            fin.setValue(parseDate(planExistente.getFechaFin()));
        } else {
            inicio.setValue(LocalDate.now());
        }
        CheckBox[] dias = new CheckBox[DayOfWeek.values().length];
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Tipo *"), tipos, new Label("Subservicio"), subservicios);
        grid.addRow(1, new Label("Inicio *"), inicio, new Label("Fin"), fin);
        grid.addRow(2, new Label("Duración (semanas)"), duracion);
        HBox diasBox = new HBox(7);
        for (DayOfWeek dia : DayOfWeek.values()) {
            CheckBox check = new CheckBox(nombreDia(dia));
            check.setUserData(dia);
            if (editar && planExistente.getDiasSemana() != null
                    && planExistente.getDiasSemana().contains(dia.name())) check.setSelected(true);
            dias[dia.getValue() - 1] = check;
            diasBox.getChildren().add(check);
        }
        grid.addRow(3, new Label("Días *"), diasBox);
        formulario.getDialogPane().setContent(grid);

        fin.valueProperty().addListener((obs, old, value) -> {
            if (value != null) duracion.clear();
        });
        duracion.textProperty().addListener((obs, old, value) -> {
            if (value != null && !value.isBlank()) fin.setValue(null);
        });
        cargarTipos(tipos, subservicios, planExistente);
        formulario.setResultConverter(boton -> {
            if (boton != guardar) return null;
            Set<DayOfWeek> seleccionados = EnumSet.noneOf(DayOfWeek.class);
            for (CheckBox check : dias) if (check.isSelected()) seleccionados.add((DayOfWeek) check.getUserData());
            if (seleccionados.isEmpty() || inicio.getValue() == null
                    || (!editar && fin.getValue() == null && duracion.getText().isBlank())
                    || (fin.getValue() != null && !duracion.getText().isBlank())) {
                estado.setText("Completa días, inicio y fecha fin o duración");
                return null;
            }
            Integer semanas = leerEntero(duracion.getText());
            if (!duracion.getText().isBlank() && semanas == null) {
                estado.setText("La duración debe ser un número entero");
                return null;
            }
            if (!editar && tipos.getValue() == null) {
                estado.setText("Selecciona un tipo de servicio");
                return null;
            }
            if (editar) {
                return new PlanServicioEdicionRequest(seleccionados, fin.getValue(), semanas);
            }
            return new PlanServicioRequest(pacienteId, tipos.getValue().getId(),
                    subservicios.getValue() == null ? null : subservicios.getValue().getId(),
                    seleccionados, inicio.getValue(), fin.getValue(), semanas);
        });
        formulario.showAndWait().ifPresent(resultado -> guardarPlan(planExistente, resultado));
    }

    private void cargarTipos(ComboBox<TipoServicioResponse> tipos, ComboBox<SubServicioResponse> subservicios,
                             PlanServicioResponse planExistente) {
        ejecutarAsync(() -> ApiClient.getInstance().getConTipoGenerico("/tipos-servicio",
                        new TypeReference<List<TipoServicioResponse>>() {}),
                lista -> {
                    tipos.setItems(FXCollections.observableArrayList(lista.stream().filter(TipoServicioResponse::isActivo).toList()));
                    tipos.valueProperty().addListener((obs, old, tipo) -> {
                        subservicios.setItems(tipo == null ? FXCollections.observableArrayList()
                                : FXCollections.observableArrayList(tipo.getSubServicios().stream()
                                .filter(SubServicioResponse::isActivo).toList()));
                    });
                    if (planExistente != null) tipos.getItems().stream()
                            .filter(t -> t.getId().equals(planExistente.getTipoServicioId()))
                            .findFirst().ifPresent(t -> {
                                tipos.setValue(t);
                                subservicios.getItems().stream()
                                        .filter(s -> s.getId().equals(planExistente.getSubServicioId()))
                                        .findFirst().ifPresent(subservicios::setValue);
                            });
                });
    }

    private void guardarPlan(PlanServicioResponse existente, Object request) {
        if (request instanceof PlanServicioRequest alta) {
            ejecutarAsync(() -> ApiClient.getInstance().post("/planes-servicio", alta,
                            PlanServicioResponse.class, true),
                    creado -> {
                        estado.setText("Plan creado y calendario generado");
                        cargarDetalle();
                    });
        } else if (request instanceof PlanServicioEdicionRequest edicion) {
            ejecutarAsync(() -> ApiClient.getInstance().put("/planes-servicio/" + existente.getId(), edicion,
                            PlanServicioResponse.class),
                    actualizado -> {
                        estado.setText("Plan actualizado");
                        cargarDetalle();
                    });
        }
    }

    private <T> void ejecutarAsync(Callable<T> llamada, Consumer<T> alExito) {
        Task<T> tarea = new Task<>() {
            @Override protected T call() throws Exception { return llamada.call(); }
        };
        tarea.setOnSucceeded(e -> alExito.accept(tarea.getValue()));
        tarea.setOnFailed(e -> {
            Throwable causa = tarea.getException();
            estado.setText(causa instanceof ApiException ? causa.getMessage() : "No se pudo completar la operación");
        });
        Thread hilo = new Thread(tarea, "crm-paciente-ficha");
        hilo.setDaemon(true);
        hilo.start();
    }

    private static String texto(String valor) { return valor == null ? "—" : valor; }

    private static Label crearEtiquetaEstado(String valor) {
        Label etiqueta = new Label(valor);
        etiqueta.getStyleClass().add("estado-" + valor.toLowerCase());
        return etiqueta;
    }

    private static Label crearEtiquetaSancion(String valor) {
        Label etiqueta = new Label(valor.replace('_', ' '));
        etiqueta.getStyleClass().add("sancion-" + valor.toLowerCase());
        return etiqueta;
    }
    private static LocalDate parseDate(String valor) {
        return valor == null ? null : LocalDate.parse(valor);
    }
    private static Integer leerEntero(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try { return Integer.valueOf(valor.trim()); } catch (NumberFormatException e) { return null; }
    }
    private static String nombreDia(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> "L";
            case TUESDAY -> "M";
            case WEDNESDAY -> "X";
            case THURSDAY -> "J";
            case FRIDAY -> "V";
            case SATURDAY -> "S";
            case SUNDAY -> "D";
        };
    }

    private static String nombreDiaCompleto(String dia) {
        return switch (DayOfWeek.valueOf(dia)) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }
}

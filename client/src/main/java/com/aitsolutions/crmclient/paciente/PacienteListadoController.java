package com.aitsolutions.crmclient.paciente;

import com.aitsolutions.crmclient.dto.PacienteResponse;
import com.aitsolutions.crmclient.dto.PacienteRequest;
import com.aitsolutions.crmclient.dto.AsociacionResponse;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.Window;

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
    @FXML private TextField campoNombre;
    @FXML private TextField campoApellidos;
    @FXML private TextField campoExpediente;
    @FXML private TextField campoDni;
    @FXML private TextField campoTelefono;
    @FXML private TextField campoEmail;
    @FXML private DatePicker campoFechaNacimiento;
    @FXML private ComboBox<String> comboGenero;
    @FXML private ComboBox<AsociacionResponse> comboAsociacion;

    @FXML
    private Button botonFicha;

    @FXML
    private void initialize() {
        configurarColumnas();
        botonFicha.setDisable(true);
        tablaPacientes.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nuevo) -> botonFicha.setDisable(nuevo == null));
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

    @FXML
    private void onCrearClick() {
        Dialog<PacienteRequest> dialogo = new Dialog<>();
        dialogo.setTitle("Registrar paciente");
        dialogo.setHeaderText("Datos del nuevo paciente");
        ButtonType registrar = new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(registrar, ButtonType.CANCEL);

        TextField nombre = new TextField(), apellidos = new TextField();
        TextField dni = new TextField(), telefono = new TextField(), email = new TextField();
        DatePicker nacimiento = new DatePicker();
        ComboBox<String> genero = new ComboBox<>(FXCollections.observableArrayList(
                "Femenino", "Masculino", "No especificado"));
        ComboBox<AsociacionResponse> asociacion = new ComboBox<>();
        dialogo.getDialogPane().lookupButton(registrar).setDisable(true);
        cargarAsociaciones(asociacion,
                () -> dialogo.getDialogPane().lookupButton(registrar).setDisable(false));
        GridPane formulario = new GridPane();
        formulario.setHgap(8); formulario.setVgap(8); formulario.setPadding(new Insets(10));
        formulario.addRow(0, new Label("Nombre *"), nombre, new Label("Apellidos *"), apellidos);
        formulario.addRow(1, new Label("DNI"), dni);
        formulario.addRow(2, new Label("Teléfono"), telefono, new Label("Email"), email);
        formulario.addRow(3, new Label("Nacimiento"), nacimiento, new Label("Género"), genero);
        formulario.addRow(4, new Label("Asociación *"), asociacion);
        dialogo.getDialogPane().setContent(formulario);
        dialogo.setResultConverter(boton -> boton == registrar && !nombre.getText().isBlank()
                && !apellidos.getText().isBlank()
                && asociacion.getValue() != null
                ? new PacienteRequest(nombre.getText(), apellidos.getText(), null,
                nacimiento.getValue() == null ? null : nacimiento.getValue().toString(), genero.getValue(),
                dni.getText(), telefono.getText(), email.getText(), asociacion.getValue().getId()) : null);
        dialogo.showAndWait().ifPresent(this::registrarPaciente);
    }

    @FXML
    private void onFichaClick() {
        PacienteResponse paciente = tablaPacientes.getSelectionModel().getSelectedItem();
        if (paciente == null) {
            etiquetaEstado.setText("Selecciona un paciente");
            return;
        }
        Window ventana = tablaPacientes.getScene() == null ? null : tablaPacientes.getScene().getWindow();
        PacienteFichaDialog.mostrar(paciente.getId(),
                paciente.getNombre() + " " + paciente.getApellidos(), ventana);
    }

    private void registrarPaciente(PacienteRequest request) {
        Task<PacienteResponse> tarea = new Task<>() {
            @Override protected PacienteResponse call() {
                return ApiClient.getInstance().post("/pacientes", request, PacienteResponse.class, true);
            }
        };
        tarea.setOnSucceeded(e -> { cargarPacientes(null); etiquetaEstado.setText("Paciente registrado"); });
        tarea.setOnFailed(e -> etiquetaEstado.setText(tarea.getException() instanceof ApiException a
                ? a.getMessage() : "No se pudo registrar el paciente"));
        new Thread(tarea).start();
    }

    private void cargarAsociaciones(ComboBox<AsociacionResponse> destino, Runnable alCompletar) {
        Task<AsociacionResponse[]> tarea = new Task<>() {
            @Override protected AsociacionResponse[] call() {
                return ApiClient.getInstance().get("/asociaciones", AsociacionResponse[].class);
            }
        };
        tarea.setOnSucceeded(e -> {
            destino.setItems(FXCollections.observableArrayList(tarea.getValue()));
            alCompletar.run();
        });
        tarea.setOnFailed(e -> etiquetaEstado.setText("No se pudieron cargar las asociaciones"));
        new Thread(tarea).start();
    }

}

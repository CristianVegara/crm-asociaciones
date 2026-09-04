package com.aitsolutions.crmclient.gestion;

import com.aitsolutions.crmclient.dto.AsignarPermisosRequest;
import com.aitsolutions.crmclient.dto.AsignarResponsablesRequest;
import com.aitsolutions.crmclient.dto.CambiarEstadoRequest;
import com.aitsolutions.crmclient.dto.CapacidadServicio;
import com.aitsolutions.crmclient.dto.PermisoResponse;
import com.aitsolutions.crmclient.dto.ResponsableItem;
import com.aitsolutions.crmclient.dto.ResponsableResponse;
import com.aitsolutions.crmclient.dto.RolRequest;
import com.aitsolutions.crmclient.dto.RolResponse;
import com.aitsolutions.crmclient.dto.TipoServicioRequest;
import com.aitsolutions.crmclient.dto.TipoServicioResponse;
import com.aitsolutions.crmclient.dto.TrabajadorRequest;
import com.aitsolutions.crmclient.dto.TrabajadorResponse;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GestionTrabajadoresController {

    @FXML
    private Label etiquetaEstado;

    // --- Trabajadores ---
    @FXML
    private TableView<TrabajadorResponse> tablaTrabajadores;
    @FXML
    private TableColumn<TrabajadorResponse, String> columnaTrabNombre;
    @FXML
    private TableColumn<TrabajadorResponse, String> columnaTrabApellidos;
    @FXML
    private TableColumn<TrabajadorResponse, String> columnaTrabUsuario;
    @FXML
    private TableColumn<TrabajadorResponse, String> columnaTrabRol;
    @FXML
    private TableColumn<TrabajadorResponse, String> columnaTrabActivo;
    @FXML
    private Button botonEstadoTrabajador;
    @FXML
    private TextField campoTrabNombre;
    @FXML
    private TextField campoTrabApellidos;
    @FXML
    private TextField campoTrabUsuario;
    @FXML
    private PasswordField campoTrabPassword;
    @FXML
    private ComboBox<RolResponse> comboTrabRol;

    // --- Roles ---
    @FXML
    private TableView<RolResponse> tablaRoles;
    @FXML
    private TableColumn<RolResponse, String> columnaRolNombre;
    @FXML
    private TableColumn<RolResponse, String> columnaRolDescripcion;
    @FXML
    private TextField campoRolNombre;
    @FXML
    private TextField campoRolDescripcion;
    @FXML
    private VBox cajaPermisos;

    // --- Servicios / responsables ---
    @FXML
    private ComboBox<TipoServicioResponse> comboTipoServicio;
    @FXML
    private TextField campoNuevoTipoServicio;
    @FXML
    private GridPane grillaResponsables;

    private final List<CheckBox> checkboxesPermisos = new ArrayList<>();
    // Clave: rolId + ":" + capacidad, para poder leer el estado de cada casilla al guardar.
    private final Map<String, CheckBox> checkboxesResponsables = new HashMap<>();

    @FXML
    private void initialize() {
        configurarColumnas();
        cargarTrabajadores();
        cargarRoles();
        cargarPermisos();
        cargarTiposServicio();

        tablaRoles.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nuevo) -> actualizarCajaPermisos(nuevo));
        botonEstadoTrabajador.setDisable(true);
        tablaTrabajadores.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nuevo) -> botonEstadoTrabajador.setDisable(nuevo == null));
    }

    private void configurarColumnas() {
        columnaTrabNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        columnaTrabApellidos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApellidos()));
        columnaTrabUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsuario()));
        columnaTrabRol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRolNombre()));
        columnaTrabActivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActivo() ? "Sí" : "No"));
        columnaTrabActivo.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                setText(empty ? null : estado);
                getStyleClass().removeIf(style -> style.startsWith("estado-"));
                if (!empty && estado != null) {
                    getStyleClass().add("estado-" + ("Sí".equals(estado) ? "verde" : "cancelada"));
                }
            }
        });

        columnaRolNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        columnaRolDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
    }

    // ---------- Carga de datos ----------

    private void cargarTrabajadores() {
        ejecutarAsync(
                () -> ApiClient.getInstance().getConTipoGenerico("/trabajadores", new TypeReference<List<TrabajadorResponse>>() {
                }),
                lista -> tablaTrabajadores.setItems(FXCollections.observableArrayList(lista))
        );
    }

    private void cargarRoles() {
        ejecutarAsync(
                () -> ApiClient.getInstance().getConTipoGenerico("/roles", new TypeReference<List<RolResponse>>() {
                }),
                lista -> {
                    tablaRoles.setItems(FXCollections.observableArrayList(lista));
                    comboTrabRol.setItems(FXCollections.observableArrayList(lista));
                    reconstruirGrillaResponsables(); // los roles son las filas de la matriz
                }
        );
    }

    private void cargarPermisos() {
        ejecutarAsync(
                () -> ApiClient.getInstance().getConTipoGenerico("/permisos", new TypeReference<List<PermisoResponse>>() {
                }),
                lista -> {
                    cajaPermisos.getChildren().clear();
                    checkboxesPermisos.clear();
                    for (PermisoResponse permiso : lista) {
                        CheckBox casilla = new CheckBox(permiso.getNombre());
                        casilla.setTooltip(new javafx.scene.control.Tooltip(permiso.getDescripcion()));
                        checkboxesPermisos.add(casilla);
                        cajaPermisos.getChildren().add(casilla);
                    }
                }
        );
    }

    private void cargarTiposServicio() {
        ejecutarAsync(
                () -> ApiClient.getInstance().getConTipoGenerico("/tipos-servicio", new TypeReference<List<TipoServicioResponse>>() {
                }),
                lista -> {
                    TipoServicioResponse seleccionActual = comboTipoServicio.getValue();
                    comboTipoServicio.setItems(FXCollections.observableArrayList(lista));
                    // Reselecciona el mismo tipo de servicio tras recargar (por id, ya que el objeto cambia de instancia).
                    if (seleccionActual != null) {
                        lista.stream().filter(t -> t.getId().equals(seleccionActual.getId())).findFirst()
                                .ifPresent(comboTipoServicio.getSelectionModel()::select);
                    }
                    reconstruirGrillaResponsables();
                }
        );
    }

    // ---------- Trabajadores ----------

    @FXML
    private void onCrearTrabajadorClick() {
        RolResponse rol = comboTrabRol.getValue();
        if (campoTrabNombre.getText().isBlank() || campoTrabApellidos.getText().isBlank()
                || campoTrabUsuario.getText().isBlank() || campoTrabPassword.getText().isBlank() || rol == null) {
            etiquetaEstado.setText("Rellena todos los campos y selecciona un rol");
            return;
        }

        TrabajadorRequest request = new TrabajadorRequest(
                campoTrabNombre.getText(), campoTrabApellidos.getText(),
                campoTrabUsuario.getText(), campoTrabPassword.getText(), rol.getId());

        ejecutarAsync(
                () -> ApiClient.getInstance().post("/trabajadores", request, TrabajadorResponse.class, true),
                creado -> {
                    etiquetaEstado.setText("Trabajador '" + creado.getUsuario() + "' creado");
                    campoTrabNombre.clear();
                    campoTrabApellidos.clear();
                    campoTrabUsuario.clear();
                    campoTrabPassword.clear();
                    cargarTrabajadores();
                }
        );
    }

    @FXML
    private void onCambiarEstadoTrabajadorClick() {
        TrabajadorResponse seleccionado = tablaTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            etiquetaEstado.setText("Selecciona un trabajador en la tabla primero");
            return;
        }

        boolean nuevoEstado = !seleccionado.isActivo();
        CambiarEstadoRequest request = new CambiarEstadoRequest(nuevoEstado);

        ejecutarAsync(
                () -> ApiClient.getInstance().patch("/trabajadores/" + seleccionado.getId() + "/estado",
                        request, TrabajadorResponse.class),
                actualizado -> {
                    etiquetaEstado.setText("Trabajador '" + actualizado.getUsuario() + "' "
                            + (actualizado.isActivo() ? "activado" : "desactivado"));
                    cargarTrabajadores();
                }
        );
    }

    // ---------- Roles ----------

    @FXML
    private void onCrearRolClick() {
        if (campoRolNombre.getText().isBlank()) {
            etiquetaEstado.setText("El nombre del rol es obligatorio");
            return;
        }
        RolRequest request = new RolRequest(campoRolNombre.getText(), campoRolDescripcion.getText());
        ejecutarAsync(
                () -> ApiClient.getInstance().post("/roles", request, RolResponse.class, true),
                creado -> {
                    etiquetaEstado.setText("Rol '" + creado.getNombre() + "' creado");
                    campoRolNombre.clear();
                    campoRolDescripcion.clear();
                    cargarRoles();
                }
        );
    }

    private void actualizarCajaPermisos(RolResponse rol) {
        for (CheckBox casilla : checkboxesPermisos) {
            casilla.setSelected(rol != null && rol.getPermisos().contains(casilla.getText()));
        }
    }

    @FXML
    private void onGuardarPermisosClick() {
        RolResponse rol = tablaRoles.getSelectionModel().getSelectedItem();
        if (rol == null) {
            etiquetaEstado.setText("Selecciona un rol en la tabla primero");
            return;
        }

        var permisosSeleccionados = checkboxesPermisos.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toSet());

        AsignarPermisosRequest request = new AsignarPermisosRequest(permisosSeleccionados);
        ejecutarAsync(
                () -> ApiClient.getInstance().put("/roles/" + rol.getId() + "/permisos", request, RolResponse.class),
                actualizado -> {
                    etiquetaEstado.setText("Permisos de '" + actualizado.getNombre() + "' guardados");
                    cargarRoles();
                }
        );
    }

    // ---------- Servicios / responsables ----------

    @FXML
    private void onCrearTipoServicioClick() {
        if (campoNuevoTipoServicio.getText().isBlank()) {
            etiquetaEstado.setText("El nombre del tipo de servicio es obligatorio");
            return;
        }
        TipoServicioRequest request = new TipoServicioRequest(campoNuevoTipoServicio.getText());
        ejecutarAsync(
                () -> ApiClient.getInstance().post("/tipos-servicio", request, TipoServicioResponse.class, true),
                creado -> {
                    etiquetaEstado.setText("Tipo de servicio '" + creado.getNombre() + "' creado");
                    campoNuevoTipoServicio.clear();
                    cargarTiposServicio();
                }
        );
    }

    @FXML
    private void onSeleccionarTipoServicio() {
        reconstruirGrillaResponsables();
    }

    private void reconstruirGrillaResponsables() {
        grillaResponsables.getChildren().clear();
        checkboxesResponsables.clear();

        TipoServicioResponse tipoServicio = comboTipoServicio.getValue();
        List<RolResponse> roles = tablaRoles.getItems();
        if (tipoServicio == null || roles.isEmpty()) {
            return;
        }

        CapacidadServicio[] capacidades = CapacidadServicio.values();

        // Cabecera
        grillaResponsables.add(new Label(""), 0, 0);
        for (int col = 0; col < capacidades.length; col++) {
            Label cabecera = new Label(formatearCapacidad(capacidades[col]));
            cabecera.setStyle("-fx-font-weight: bold;");
            grillaResponsables.add(cabecera, col + 1, 0);
        }

        for (int fila = 0; fila < roles.size(); fila++) {
            RolResponse rol = roles.get(fila);
            grillaResponsables.add(new Label(rol.getNombre()), 0, fila + 1);

            for (int col = 0; col < capacidades.length; col++) {
                CapacidadServicio capacidad = capacidades[col];
                boolean yaAsignado = tipoServicio.getResponsables().stream()
                        .anyMatch(r -> r.getRolId().equals(rol.getId()) && r.getCapacidad() == capacidad);

                CheckBox casilla = new CheckBox();
                casilla.setSelected(yaAsignado);
                casilla.setAlignment(Pos.CENTER);
                checkboxesResponsables.put(rol.getId() + ":" + capacidad, casilla);
                grillaResponsables.add(casilla, col + 1, fila + 1);
            }
        }
    }

    private String formatearCapacidad(CapacidadServicio capacidad) {
        return switch (capacidad) {
            case REGISTRAR_ASISTENCIA -> "Registrar asistencia";
            case APLICAR_SANCION -> "Aplicar sanción";
            case GESTIONAR_PLAN -> "Gestionar plan";
        };
    }

    @FXML
    private void onGuardarResponsablesClick() {
        TipoServicioResponse tipoServicio = comboTipoServicio.getValue();
        if (tipoServicio == null) {
            etiquetaEstado.setText("Selecciona un tipo de servicio primero");
            return;
        }

        List<ResponsableItem> items = new ArrayList<>();
        checkboxesResponsables.forEach((clave, casilla) -> {
            if (casilla.isSelected()) {
                String[] partes = clave.split(":");
                items.add(new ResponsableItem(Long.valueOf(partes[0]), CapacidadServicio.valueOf(partes[1])));
            }
        });

        AsignarResponsablesRequest request = new AsignarResponsablesRequest(items);
        ejecutarAsync(
                () -> ApiClient.getInstance().put("/tipos-servicio/" + tipoServicio.getId() + "/responsables",
                        request, TipoServicioResponse.class),
                actualizado -> {
                    etiquetaEstado.setText("Responsables de '" + actualizado.getNombre() + "' guardados");
                    cargarTiposServicio();
                }
        );
    }

    // ---------- Helper de llamadas asíncronas ----------

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

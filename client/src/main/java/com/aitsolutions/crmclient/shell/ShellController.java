package com.aitsolutions.crmclient.shell;

import com.aitsolutions.crmclient.MainApp;
import com.aitsolutions.crmclient.sesion.SesionActiva;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShellController {

    private static final Logger LOG = Logger.getLogger(ShellController.class.getName());
    @FXML
    private Button botonPacientes;
    @FXML
    private Button botonPlanes;
    @FXML
    private Button botonAgenda;

    @FXML
    private Button botonSanciones;

    @FXML
    private Button botonTrabajadores;

    @FXML
    private Button botonInformes;

    @FXML
    private Label etiquetaSesion;

    @FXML
    private StackPane contenedorContenido;

    private static final String CLASE_BOTON_ACTIVO = "nav-button-active";

    @FXML
    private void initialize() {
        SesionActiva sesion = SesionActiva.getInstance();
        etiquetaSesion.setText(sesion.getNombreCompleto() + " · " + sesion.getRolNombre());

        boolean vePacientes = sesion.tienePermiso("GESTIONAR_PACIENTES");
        boolean vePlanes = sesion.tienePermiso("CREAR_PLAN_SERVICIO");
        boolean veAgenda = sesion.tienePermiso("REGISTRAR_ASISTENCIA");
        boolean veSanciones = sesion.tienePermiso("APLICAR_SANCION");
        boolean veTrabajadores = sesion.tienePermiso("GESTIONAR_TRABAJADORES");
        boolean veInformes = sesion.tienePermiso("VER_INFORMES");

        configurarVisibilidad(botonPacientes, vePacientes);
        configurarVisibilidad(botonPlanes, vePlanes);
        configurarVisibilidad(botonAgenda, veAgenda);
        configurarVisibilidad(botonSanciones, veSanciones);
        configurarVisibilidad(botonTrabajadores, veTrabajadores);
        configurarVisibilidad(botonInformes, veInformes);

        // Aterriza en el primer modulo al que el trabajador tenga acceso.
        if (vePacientes) {
            cargarModulo("paciente-listado.fxml", botonPacientes);
        } else if (vePlanes) {
            cargarModulo("plan-servicio-screen.fxml", botonPlanes);
        } else if (veAgenda) {
            cargarModulo("agenda-screen.fxml", botonAgenda);
        } else if (veSanciones) {
            cargarModulo("sancion-screen.fxml", botonSanciones);
        } else if (veTrabajadores) {
            cargarModulo("gestion-trabajadores.fxml", botonTrabajadores);
        } else if (veInformes) {
            cargarModulo("informe-screen.fxml", botonInformes);
        } else {
            cargarModulo("bienvenida.fxml", null);
        }

    }

    private void configurarVisibilidad(Button boton, boolean visible) {
        boton.setVisible(visible);
        boton.setManaged(visible);
    }

    @FXML
    private void onPacientesClick() {
        cargarModulo("paciente-listado.fxml", botonPacientes);
    }

    @FXML
    private void onPlanesClick() {
        cargarModulo("plan-servicio-screen.fxml", botonPlanes);
    }

    @FXML
    private void onAgendaClick() {
        cargarModulo("agenda-screen.fxml", botonAgenda);
    }

    @FXML
    private void onSancionesClick() {
        cargarModulo("sancion-screen.fxml", botonSanciones);
    }

    @FXML
    private void onTrabajadoresClick() {
        cargarModulo("gestion-trabajadores.fxml", botonTrabajadores);
    }

    @FXML
    private void onInformesClick() {
        contenedorContenido.getChildren().setAll(new Label("Cargando informes..."));
        cargarModulo("informe-screen.fxml", botonInformes);
    }

    @FXML
    private void onCerrarSesionClick() throws IOException {
        SesionActiva.getInstance().cerrar();
        MainApp.mostrarLogin();
    }

    private void cargarModulo(String fxml, Button botonActivo) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxml));
            Parent nodo = loader.load();
            contenedorContenido.getChildren().setAll(nodo);
            marcarBotonActivo(botonActivo);
        } catch (IOException e) {
            mostrarErrorCarga(fxml, e);
        } catch (RuntimeException e) {
            mostrarErrorCarga(fxml, e);
        }
    }

    private void mostrarErrorCarga(String fxml, Exception error) {
        LOG.log(Level.SEVERE, "No se pudo cargar la pantalla " + fxml, error);
        Label mensaje = new Label("No se pudo abrir el módulo solicitado: " + fxml
                + "\nRevisa la consola para ver el detalle del error.");
        mensaje.setWrapText(true);
        contenedorContenido.getChildren().setAll(mensaje);
    }

    private void marcarBotonActivo(Button botonActivo) {
        for (Button boton : new Button[]{botonPacientes, botonPlanes, botonAgenda, botonSanciones, botonTrabajadores, botonInformes}) {
            boton.getStyleClass().remove(CLASE_BOTON_ACTIVO);
        }
        if (botonActivo != null) {
            botonActivo.getStyleClass().add(CLASE_BOTON_ACTIVO);
        }
    }
}

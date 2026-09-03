package com.aitsolutions.crmclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada del cliente. Tras el login, la navegacion entre modulos (Pacientes,
 * Sanciones, Trabajadores y roles) ocurre DENTRO de una unica escena (shell.fxml, con
 * barra superior persistente) en vez de reemplazar la escena completa por cada pantalla:
 * asi el usuario no pierde el contexto de "donde estoy" ni tiene que volver a un menu
 * intermedio para cambiar de modulo.
 */
public class MainApp extends Application {

    private static Stage stagePrincipal;

    @Override
    public void start(Stage stage) throws IOException {
        stagePrincipal = stage;
        stage.setTitle("CRM Asociaciones");
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("logo.png")));
        mostrarLogin();
        stage.show();
    }

    public static void mostrarLogin() throws IOException {
        cambiarVista("login.fxml", 380, 360);
    }

    public static void mostrarAplicacionPrincipal() throws IOException {
        cambiarVista("shell.fxml", 1000, 650);
    }

    private static void cambiarVista(String fxml, double ancho, double alto) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxml));
        Parent raiz = loader.load();
        Scene escena = new Scene(raiz, ancho, alto);
        escena.getStylesheets().add(MainApp.class.getResource("styles.css").toExternalForm());
        stagePrincipal.setScene(escena);
        stagePrincipal.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

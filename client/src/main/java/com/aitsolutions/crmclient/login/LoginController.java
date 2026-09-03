package com.aitsolutions.crmclient.login;

import com.aitsolutions.crmclient.MainApp;
import com.aitsolutions.crmclient.dto.LoginRequest;
import com.aitsolutions.crmclient.dto.LoginResponse;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import com.aitsolutions.crmclient.sesion.SesionActiva;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField campoUsuario;

    @FXML
    private PasswordField campoPassword;

    @FXML
    private Button botonLogin;

    @FXML
    private Label etiquetaError;

    @FXML
    private void onLoginClick() {
        String usuario = campoUsuario.getText();
        String password = campoPassword.getText();

        if (usuario.isBlank() || password.isBlank()) {
            etiquetaError.setText("Introduce usuario y contraseña");
            return;
        }

        etiquetaError.setText("");
        botonLogin.setDisable(true);

        // Llamada de red en un hilo aparte: si se hiciera en el hilo de JavaFX,
        // la ventana se congelaria mientras espera al backend.
        Task<LoginResponse> tareaLogin = new Task<>() {
            @Override
            protected LoginResponse call() {
                LoginRequest request = new LoginRequest(usuario, password);
                return ApiClient.getInstance().post("/auth/login", request, LoginResponse.class, false);
            }
        };

        tareaLogin.setOnSucceeded(evento -> {
            SesionActiva.getInstance().iniciar(tareaLogin.getValue());
            botonLogin.setDisable(false);
            navegarAlMenu();
        });

        tareaLogin.setOnFailed(evento -> {
            botonLogin.setDisable(false);
            Throwable causa = tareaLogin.getException();
            String mensaje = (causa instanceof ApiException apiException)
                    ? apiException.getMessage()
                    : "No se pudo iniciar sesión. Inténtalo de nuevo.";
            etiquetaError.setText(mensaje);
        });

        new Thread(tareaLogin).start();
    }

    private void navegarAlMenu() {
        try {
            MainApp.mostrarAplicacionPrincipal();
        } catch (Exception e) {
            etiquetaError.setText("Error abriendo la siguiente pantalla: " + e.getMessage());
        }
    }
}

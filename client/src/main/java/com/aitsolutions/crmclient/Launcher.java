package com.aitsolutions.crmclient;

import javafx.application.Application;

/**
 * Punto de entrada del ejecutable empaquetado. Un launcher normal evita que
 * jpackage intente tratar la clase JavaFX Application como una clase main.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}

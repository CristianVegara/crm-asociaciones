package com.aitsolutions.crmclient.http;

/**
 * Envuelve cualquier fallo al hablar con el backend: código HTTP de error, JSON
 * inesperado, o el servidor inalcanzable. El mensaje ya viene listo para mostrar
 * directamente en la UI.
 */
public class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

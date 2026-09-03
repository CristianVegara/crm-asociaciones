package com.aitsolutions.crm.common;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Cuerpo de respuesta uniforme para cualquier error de la API.
 */
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String message;
    private final Map<String, String> fieldErrors;

    public ErrorResponse(int status, String message) {
        this(status, message, null);
    }

    public ErrorResponse(int status, String message, Map<String, String> fieldErrors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}

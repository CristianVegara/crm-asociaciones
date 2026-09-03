package com.aitsolutions.crm.common;

/**
 * Se lanza cuando se busca por id una entidad que no existe.
 * La captura {@link GlobalExceptionHandler} y la traduce a un 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

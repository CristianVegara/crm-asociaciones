package com.aitsolutions.crm.auth;

/**
 * Se lanza tanto si el usuario no existe, esta inactivo o la contraseña no coincide.
 * Se usa siempre el mismo mensaje generico para no revelar cual de los tres fue el motivo
 * (evita dar pistas a quien intenta adivinar usuarios validos).
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos");
    }
}

package com.aitsolutions.crmclient.dto;

public record PacienteRequest(String nombre, String apellidos, String numeroExpediente,
                              String fechaNacimiento, String genero, String dni,
                              String telefono, String email, Long asociacionId) {
}

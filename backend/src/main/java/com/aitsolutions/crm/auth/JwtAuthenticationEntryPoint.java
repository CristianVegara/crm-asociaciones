package com.aitsolutions.crm.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aitsolutions.crm.common.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Por defecto, Spring Security devuelve 403 tanto si falta el token como si el permiso
 * no alcanza (trata las peticiones sin token como "usuario anonimo" en vez de "no autenticado").
 * Este componente corrige eso: 401 cuando no hay autenticacion valida, dejando el 403
 * (gestionado aparte, ver SecurityConfig) solo para cuando el token es valido pero el
 * permiso no es suficiente.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "No autenticado: falta el token o no es válido"
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

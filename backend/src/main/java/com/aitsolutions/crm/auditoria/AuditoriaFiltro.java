package com.aitsolutions.crm.auditoria;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuditoriaFiltro extends OncePerRequestFilter {
    private final AuditoriaEventoRepository repository;

    public AuditoriaFiltro(AuditoriaEventoRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
            String usuario = autenticacion == null || !autenticacion.isAuthenticated()
                    ? "anonimo" : autenticacion.getName();
            repository.save(new AuditoriaEvento(usuario, request.getMethod(),
                    request.getRequestURI(), response.getStatus(), request.getRemoteAddr()));
        }
    }
}

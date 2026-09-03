package com.aitsolutions.crm.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee la cabecera "Authorization: Bearer <token>", valida el JWT y, si es correcto,
 * deja al trabajador autenticado en el SecurityContext con sus permisos como authorities.
 * No consulta la base de datos: todo lo necesario (usuario, permisos) va dentro del token
 * (ver JwtService). Si el token falta, es invalido o ha expirado, simplemente no autentica
 * y deja que SecurityConfig rechace la peticion con 401 si el endpoint lo requiere.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith(PREFIJO_BEARER)) {
            String token = cabecera.substring(PREFIJO_BEARER.length());
            try {
                Claims claims = jwtService.validarYObtenerClaims(token);
                autenticar(claims);
            } catch (JwtException e) {
                // Token invalido/expirado: no se autentica. El endpoint protegido devolvera 401.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void autenticar(Claims claims) {
        String usuario = claims.getSubject();
        List<String> permisos = jwtService.extraerPermisos(claims);

        List<SimpleGrantedAuthority> authorities = permisos.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(usuario, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

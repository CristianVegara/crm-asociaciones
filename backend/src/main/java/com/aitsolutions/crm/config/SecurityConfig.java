package com.aitsolutions.crm.config;

import com.aitsolutions.crm.auth.JwtAuthenticationEntryPoint;
import com.aitsolutions.crm.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.aitsolutions.crm.auditoria.AuditoriaFiltro;

/**
 * Autenticacion 100% via JWT (ver auth/JwtAuthenticationFilter): no usamos el
 * AuthenticationManager/AuthenticationProvider estandar de Spring Security, por eso
 * no hay formLogin ni httpBasic. @EnableMethodSecurity habilita @PreAuthorize en los
 * controladores para comprobar permisos concretos (GESTIONAR_TRABAJADORES, etc.).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final AuditoriaFiltro auditoriaFiltro;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                           JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                           AuditoriaFiltro auditoriaFiltro) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.auditoriaFiltro = auditoriaFiltro;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API sin estado consumida por el cliente JavaFX, no por navegador con cookies
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(auditoriaFiltro, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean "vacio" solo para que Spring Boot no auto-configure su propio usuario en memoria
     * con contraseña aleatoria (UserDetailsServiceAutoConfiguration se activa si no encuentra
     * ningun bean de este tipo). No se usa en ningun flujo real: la autenticacion la resuelve
     * JwtAuthenticationFilter directamente a partir del token.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return usuario -> {
            throw new UsernameNotFoundException(
                    "No se usa autenticacion por UserDetailsService, solo por JWT");
        };
    }
}

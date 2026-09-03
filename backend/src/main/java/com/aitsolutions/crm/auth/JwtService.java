package com.aitsolutions.crm.auth;

import com.aitsolutions.crm.permiso.Permiso;
import com.aitsolutions.crm.trabajador.Trabajador;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Genera y valida los JWT de sesion. Los permisos del trabajador se incluyen como claim
 * dentro del propio token: asi el filtro (JwtAuthenticationFilter) puede autorizar cada
 * peticion sin volver a consultar la base de datos. Si el director cambia los permisos
 * de un rol, los trabajadores con sesion activa no lo notaran hasta volver a hacer login
 * (aceptable para el MVP; revisar si en producción hiciera falta invalidacion inmediata).
 */
@Service
public class JwtService {

    private static final String CLAIM_PERMISOS = "permisos";
    private static final String CLAIM_TRABAJADOR_ID = "trabajadorId";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secreto,
                       @Value("${jwt.expiration-ms}") long expiracionMs) {
        // El secreto de application.properties se usa directamente como bytes UTF-8,
        // no como base64 (mas simple de configurar para este proyecto).
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(Trabajador trabajador) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        List<String> permisos = trabajador.getRol().getPermisos().stream()
                .map(Permiso::name)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(trabajador.getUsuario())
                .claim(CLAIM_TRABAJADOR_ID, trabajador.getId())
                .claim(CLAIM_PERMISOS, permisos)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(clave)
                .compact();
    }

    /**
     * @throws io.jsonwebtoken.JwtException si el token es invalido, ha expirado o la firma no coincide.
     */
    public Claims validarYObtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpiracionSegundos() {
        return expiracionMs / 1000;
    }

    @SuppressWarnings("unchecked")
    public List<String> extraerPermisos(Claims claims) {
        return claims.get(CLAIM_PERMISOS, List.class);
    }
}

package com.aitsolutions.crm.auth;

import com.aitsolutions.crm.trabajador.Trabajador;
import com.aitsolutions.crm.trabajador.TrabajadorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final TrabajadorRepository trabajadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(TrabajadorRepository trabajadorRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.trabajadorRepository = trabajadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Trabajador trabajador = trabajadorRepository.findByUsuario(request.getUsuario())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!trabajador.isActivo()) {
            throw new CredencialesInvalidasException();
        }

        if (!passwordEncoder.matches(request.getPassword(), trabajador.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        String token = jwtService.generarToken(trabajador);
        return new LoginResponse(token, jwtService.getExpiracionSegundos(), trabajador);
    }
}

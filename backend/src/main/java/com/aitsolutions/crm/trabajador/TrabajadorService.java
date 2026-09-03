package com.aitsolutions.crm.trabajador;

import com.aitsolutions.crm.common.ResourceNotFoundException;
import com.aitsolutions.crm.rol.Rol;
import com.aitsolutions.crm.rol.RolService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrabajadorService {

    private final TrabajadorRepository trabajadorRepository;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;

    public TrabajadorService(TrabajadorRepository trabajadorRepository,
                              RolService rolService,
                              PasswordEncoder passwordEncoder) {
        this.trabajadorRepository = trabajadorRepository;
        this.rolService = rolService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Trabajador> listarTodos() {
        return trabajadorRepository.findAll();
    }

    public Trabajador buscarPorId(Long id) {
        return trabajadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el trabajador con id " + id));
    }

    public Trabajador crear(TrabajadorRequest request) {
        if (trabajadorRepository.existsByUsuario(request.getUsuario())) {
            throw new IllegalArgumentException("Ya existe un trabajador con el usuario " + request.getUsuario());
        }
        Rol rol = rolService.buscarPorId(request.getRolId());
        String hash = passwordEncoder.encode(request.getPassword());
        Trabajador trabajador = new Trabajador(request.getNombre(), request.getApellidos(),
                request.getUsuario(), hash, rol);
        return trabajadorRepository.save(trabajador);
    }

    public Trabajador actualizar(Long id, TrabajadorRequest request) {
        Trabajador trabajador = buscarPorId(id);
        Rol rol = rolService.buscarPorId(request.getRolId());

        trabajador.setNombre(request.getNombre());
        trabajador.setApellidos(request.getApellidos());
        trabajador.setUsuario(request.getUsuario());
        trabajador.setRol(rol);

        // Solo se re-hashea si se ha enviado una contraseña nueva (evita forzar cambio en cada edicion).
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            trabajador.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return trabajadorRepository.save(trabajador);
    }

    public Trabajador cambiarEstado(Long id, boolean activo) {
        Trabajador trabajador = buscarPorId(id);
        trabajador.setActivo(activo);
        return trabajadorRepository.save(trabajador);
    }
}

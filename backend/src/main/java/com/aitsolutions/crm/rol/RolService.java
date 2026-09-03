package com.aitsolutions.crm.rol;

import com.aitsolutions.crm.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }

    public Rol buscarPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el rol con id " + id));
    }

    public Rol crear(RolRequest request) {
        Rol rol = new Rol(request.getNombre(), request.getDescripcion());
        return rolRepository.save(rol);
    }

    public Rol actualizar(Long id, RolRequest request) {
        Rol rol = buscarPorId(id);
        rol.setNombre(request.getNombre());
        rol.setDescripcion(request.getDescripcion());
        return rolRepository.save(rol);
    }

    public Rol asignarPermisos(Long id, AsignarPermisosRequest request) {
        Rol rol = buscarPorId(id);
        rol.setPermisos(request.getPermisos());
        return rolRepository.save(rol);
    }
}

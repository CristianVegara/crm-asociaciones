package com.aitsolutions.crm.asociacion;

import com.aitsolutions.crm.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsociacionService {

    private final AsociacionRepository asociacionRepository;

    public AsociacionService(AsociacionRepository asociacionRepository) {
        this.asociacionRepository = asociacionRepository;
    }

    public List<Asociacion> listarTodas() {
        return asociacionRepository.findAll();
    }

    public Asociacion buscarPorId(Long id) {
        return asociacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la asociacion con id " + id));
    }

    public Asociacion crear(AsociacionRequest request) {
        Asociacion asociacion = new Asociacion(request.getNombre(), request.getDireccion(), request.getContacto());
        return asociacionRepository.save(asociacion);
    }

    public Asociacion actualizar(Long id, AsociacionRequest request) {
        Asociacion asociacion = buscarPorId(id);
        asociacion.setNombre(request.getNombre());
        asociacion.setDireccion(request.getDireccion());
        asociacion.setContacto(request.getContacto());
        return asociacionRepository.save(asociacion);
    }
}

package com.aitsolutions.crm.paciente;

import com.aitsolutions.crm.asociacion.Asociacion;
import com.aitsolutions.crm.asociacion.AsociacionService;
import com.aitsolutions.crm.common.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final AsociacionService asociacionService;

    public PacienteService(PacienteRepository pacienteRepository, AsociacionService asociacionService) {
        this.pacienteRepository = pacienteRepository;
        this.asociacionService = asociacionService;
    }

    public Page<Paciente> buscar(Long asociacionId, String nombre, Pageable pageable) {
        Specification<Paciente> filtro = Specification
                .where(PacienteSpecifications.conAsociacion(asociacionId))
                .and(PacienteSpecifications.conNombreQueContenga(nombre));
        return pacienteRepository.findAll(filtro, pageable);
    }

    public Paciente buscarPorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el paciente con id " + id));
    }

    public Paciente crear(PacienteRequest request) {
        if (pacienteRepository.existsByNumeroExpediente(request.getNumeroExpediente())) {
            throw new IllegalArgumentException(
                    "Ya existe un paciente con el numero de expediente " + request.getNumeroExpediente());
        }
        Asociacion asociacion = asociacionService.buscarPorId(request.getAsociacionId());
        Paciente paciente = new Paciente(
                request.getNombre(),
                request.getApellidos(),
                request.getNumeroExpediente(),
                request.getFechaNacimiento(),
                request.getGenero(),
                asociacion
        );
        paciente.setDni(request.getDni());
        paciente.setTelefono(request.getTelefono());
        paciente.setEmail(request.getEmail());
        return pacienteRepository.save(paciente);
    }

    public Paciente actualizar(Long id, PacienteRequest request) {
        Paciente paciente = buscarPorId(id);
        Asociacion asociacion = asociacionService.buscarPorId(request.getAsociacionId());

        paciente.setNombre(request.getNombre());
        paciente.setApellidos(request.getApellidos());
        paciente.setNumeroExpediente(request.getNumeroExpediente());
        paciente.setFechaNacimiento(request.getFechaNacimiento());
        paciente.setGenero(request.getGenero());
        paciente.setDni(request.getDni());
        paciente.setTelefono(request.getTelefono());
        paciente.setEmail(request.getEmail());
        paciente.setAsociacion(asociacion);

        return pacienteRepository.save(paciente);
    }

    public Paciente cambiarEstado(Long id, boolean activo) {
        Paciente paciente = buscarPorId(id);
        paciente.setActivo(activo);
        return pacienteRepository.save(paciente);
    }
}

package com.aitsolutions.crm.paciente;

import com.aitsolutions.crm.asociacion.Asociacion;
import com.aitsolutions.crm.asociacion.AsociacionService;
import com.aitsolutions.crm.common.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PacienteService {

    private static final Pattern EXPEDIENTE_GENERADO =
            Pattern.compile("([A-Z])E-(\\d{4})-(\\d{3})");

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

    public synchronized Paciente crear(PacienteRequest request) {
        Asociacion asociacion = asociacionService.buscarPorId(request.getAsociacionId());
        String numeroExpediente = siguienteNumeroExpediente();
        Paciente paciente = new Paciente(
                request.getNombre(),
                request.getApellidos(),
                numeroExpediente,
                request.getFechaNacimiento(),
                request.getGenero(),
                asociacion
        );
        paciente.setDni(request.getDni());
        paciente.setTelefono(request.getTelefono());
        paciente.setEmail(request.getEmail());
        return pacienteRepository.save(paciente);
    }

    private String siguienteNumeroExpediente() {
        int year = Year.now().getValue();
        char letra = 'M';
        int secuencia = 0;

        for (Paciente paciente : pacienteRepository.findAll()) {
            Matcher matcher = EXPEDIENTE_GENERADO.matcher(paciente.getNumeroExpediente());
            if (!matcher.matches() || Integer.parseInt(matcher.group(2)) != year) {
                continue;
            }
            char letraEncontrada = matcher.group(1).charAt(0);
            int secuenciaEncontrada = Integer.parseInt(matcher.group(3));
            if (letraEncontrada > letra
                    || (letraEncontrada == letra && secuenciaEncontrada > secuencia)) {
                letra = letraEncontrada;
                secuencia = secuenciaEncontrada;
            }
        }

        if (secuencia == 999) {
            letra++;
            secuencia = 1;
        } else {
            secuencia++;
        }
        if (letra > 'Z') {
            throw new IllegalStateException("Se ha agotado el rango de letras para expedientes del año " + year);
        }
        return String.format("%cE-%d-%03d", letra, year, secuencia);
    }

    public Paciente actualizar(Long id, PacienteRequest request) {
        Paciente paciente = buscarPorId(id);
        Asociacion asociacion = asociacionService.buscarPorId(request.getAsociacionId());

        paciente.setNombre(request.getNombre());
        paciente.setApellidos(request.getApellidos());
        if (request.getNumeroExpediente() != null && !request.getNumeroExpediente().isBlank()) {
            paciente.setNumeroExpediente(request.getNumeroExpediente());
        }
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

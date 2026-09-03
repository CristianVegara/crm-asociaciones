package com.aitsolutions.crm.tiposervicio;

import com.aitsolutions.crm.common.ResourceNotFoundException;
import com.aitsolutions.crm.rol.Rol;
import com.aitsolutions.crm.rol.RolService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipoServicioService {

    private final TipoServicioRepository tipoServicioRepository;
    private final SubServicioRepository subServicioRepository;
    private final TipoServicioResponsableRepository tipoServicioResponsableRepository;
    private final RolService rolService;

    public TipoServicioService(TipoServicioRepository tipoServicioRepository,
                                SubServicioRepository subServicioRepository,
                                TipoServicioResponsableRepository tipoServicioResponsableRepository,
                                RolService rolService) {
        this.tipoServicioRepository = tipoServicioRepository;
        this.subServicioRepository = subServicioRepository;
        this.tipoServicioResponsableRepository = tipoServicioResponsableRepository;
        this.rolService = rolService;
    }

    public List<TipoServicio> listarTodos() {
        return tipoServicioRepository.findAll();
    }

    public TipoServicio buscarPorId(Long id) {
        return tipoServicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el tipo de servicio con id " + id));
    }

    public List<TipoServicioResponsable> obtenerResponsables(TipoServicio tipoServicio) {
        return tipoServicioResponsableRepository.findByTipoServicio(tipoServicio);
    }

    public TipoServicio crear(TipoServicioRequest request) {
        TipoServicio tipoServicio = new TipoServicio(request.getNombre(), request.getIcono(), request.getColor());
        return tipoServicioRepository.save(tipoServicio);
    }

    public TipoServicio actualizar(Long id, TipoServicioRequest request) {
        TipoServicio tipoServicio = buscarPorId(id);
        tipoServicio.setNombre(request.getNombre());
        tipoServicio.setIcono(request.getIcono());
        tipoServicio.setColor(request.getColor());
        return tipoServicioRepository.save(tipoServicio);
    }

    public TipoServicio agregarSubServicio(Long tipoServicioId, SubServicioRequest request) {
        TipoServicio tipoServicio = buscarPorId(tipoServicioId);
        tipoServicio.agregarSubServicio(new SubServicio(request.getNombre()));
        return tipoServicioRepository.save(tipoServicio);
    }

    public SubServicio actualizarSubServicio(Long subServicioId, SubServicioRequest request) {
        SubServicio subServicio = subServicioRepository.findById(subServicioId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el subservicio con id " + subServicioId));
        subServicio.setNombre(request.getNombre());
        return subServicioRepository.save(subServicio);
    }

    /**
     * Reemplaza toda la matriz de responsables de un tipo de servicio (borra todo lo anterior
     * y crea de nuevo la lista recibida), igual que RolService.asignarPermisos hace con los
     * permisos generales de un rol.
     */
    @Transactional
    public List<TipoServicioResponsable> asignarResponsables(Long tipoServicioId, List<ResponsableItem> items) {
        TipoServicio tipoServicio = buscarPorId(tipoServicioId);
        tipoServicioResponsableRepository.deleteByTipoServicio(tipoServicio);

        List<TipoServicioResponsable> nuevos = items.stream()
                .map(item -> {
                    Rol rol = rolService.buscarPorId(item.getRolId());
                    return new TipoServicioResponsable(tipoServicio, rol, item.getCapacidad());
                })
                .toList();

        return tipoServicioResponsableRepository.saveAll(nuevos);
    }

    /**
     * Usado por PlanServicioService para validar que un subservicio pertenece
     * realmente al tipo de servicio indicado en el plan.
     */
    public SubServicio buscarSubServicioPorId(Long id) {
        return subServicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el subservicio con id " + id));
    }
}

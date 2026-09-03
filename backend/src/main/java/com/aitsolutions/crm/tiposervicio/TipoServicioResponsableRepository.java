package com.aitsolutions.crm.tiposervicio;

import com.aitsolutions.crm.rol.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoServicioResponsableRepository extends JpaRepository<TipoServicioResponsable, Long> {

    List<TipoServicioResponsable> findByTipoServicio(TipoServicio tipoServicio);

    void deleteByTipoServicio(TipoServicio tipoServicio);

    boolean existsByTipoServicioAndRolAndCapacidad(TipoServicio tipoServicio, Rol rol, CapacidadServicio capacidad);
}

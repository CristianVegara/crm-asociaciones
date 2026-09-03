package com.aitsolutions.crm.tiposervicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AsignarResponsablesRequest {

    @NotNull(message = "La lista de responsables es obligatoria (puede estar vacía)")
    @Valid
    private List<ResponsableItem> responsables;

    public List<ResponsableItem> getResponsables() {
        return responsables;
    }

    public void setResponsables(List<ResponsableItem> responsables) {
        this.responsables = responsables;
    }
}

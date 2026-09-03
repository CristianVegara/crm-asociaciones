package com.aitsolutions.crmclient.dto;

import java.util.List;

public class AsignarResponsablesRequest {

    private List<ResponsableItem> responsables;

    public AsignarResponsablesRequest(List<ResponsableItem> responsables) {
        this.responsables = responsables;
    }

    public List<ResponsableItem> getResponsables() {
        return responsables;
    }

    public void setResponsables(List<ResponsableItem> responsables) {
        this.responsables = responsables;
    }
}

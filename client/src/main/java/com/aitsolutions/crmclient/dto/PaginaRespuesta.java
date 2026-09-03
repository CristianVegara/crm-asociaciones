package com.aitsolutions.crmclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Spring Data devuelve un objeto Page con muchos campos (pageable, sort, first, last...).
 * El cliente, por ahora, solo necesita el contenido y el total; @JsonIgnoreProperties
 * descarta el resto sin que Jackson falle al deserializar.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginaRespuesta<T> {

    private List<T> content;
    private long totalElements;

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
}

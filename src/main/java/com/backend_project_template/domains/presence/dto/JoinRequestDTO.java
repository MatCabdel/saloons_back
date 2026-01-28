package com.backend_project_template.domains.presence.dto;

/**
 * DTO pour la requête de join d'un saloon.
 */
public class JoinRequestDTO {
    private Double lat;
    private Double lng;

    public JoinRequestDTO() {
    }

    public JoinRequestDTO(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}

package com.backend_project_template.domains.presence;

import com.backend_project_template.domains.saloon.Saloon;

import java.math.BigDecimal;

/**
 * DTO pour les saloons affichés sur la carte.
 * Contient les infos de base + présence temps réel.
 */
public class SaloonMapDTO {
    private Long id;
    private String name;
    private String imgUrl;
    private String address;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer radiusMeters;
    private Integer distanceMeters; // Distance par rapport à l'utilisateur (nullable)
    private int connectedCount; // Nombre d'utilisateurs connectés

    public SaloonMapDTO() {
    }

    public SaloonMapDTO(Saloon saloon, Integer distanceMeters, int connectedCount) {
        this.id = saloon.getId();
        this.name = saloon.getName();
        this.imgUrl = saloon.getImgUrl();
        this.address = saloon.getAddress();
        this.city = saloon.getCity();
        this.latitude = saloon.getLatitude();
        this.longitude = saloon.getLongitude();
        this.radiusMeters = saloon.getRadiusMeters();
        this.distanceMeters = distanceMeters;
        this.connectedCount = connectedCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Integer getRadiusMeters() {
        return radiusMeters;
    }

    public void setRadiusMeters(Integer radiusMeters) {
        this.radiusMeters = radiusMeters;
    }

    public Integer getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Integer distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public int getConnectedCount() {
        return connectedCount;
    }

    public void setConnectedCount(int connectedCount) {
        this.connectedCount = connectedCount;
    }
}

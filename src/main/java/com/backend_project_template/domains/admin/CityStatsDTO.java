package com.backend_project_template.domains.admin;

import java.util.Map;

public class CityStatsDTO {
    private Map<String, Long> usersByCity;
    private Map<String, Long> connectedUsersByCity;

    public CityStatsDTO() {
    }

    public CityStatsDTO(Map<String, Long> usersByCity, Map<String, Long> connectedUsersByCity) {
        this.usersByCity = usersByCity;
        this.connectedUsersByCity = connectedUsersByCity;
    }

    public Map<String, Long> getUsersByCity() {
        return usersByCity;
    }

    public void setUsersByCity(Map<String, Long> usersByCity) {
        this.usersByCity = usersByCity;
    }

    public Map<String, Long> getConnectedUsersByCity() {
        return connectedUsersByCity;
    }

    public void setConnectedUsersByCity(Map<String, Long> connectedUsersByCity) {
        this.connectedUsersByCity = connectedUsersByCity;
    }
}

package com.backend_project_template.domains.admin;

import java.util.List;
import java.util.Map;

public class SaloonsByCityStatsDTO {
    private Map<String, Long> saloonCountByCity;
    private Map<String, List<SaloonStatsItemDTO>> saloonsByCity;

    public SaloonsByCityStatsDTO() {
    }

    public SaloonsByCityStatsDTO(Map<String, Long> saloonCountByCity,
            Map<String, List<SaloonStatsItemDTO>> saloonsByCity) {
        this.saloonCountByCity = saloonCountByCity;
        this.saloonsByCity = saloonsByCity;
    }

    public Map<String, Long> getSaloonCountByCity() {
        return saloonCountByCity;
    }

    public void setSaloonCountByCity(Map<String, Long> saloonCountByCity) {
        this.saloonCountByCity = saloonCountByCity;
    }

    public Map<String, List<SaloonStatsItemDTO>> getSaloonsByCity() {
        return saloonsByCity;
    }

    public void setSaloonsByCity(Map<String, List<SaloonStatsItemDTO>> saloonsByCity) {
        this.saloonsByCity = saloonsByCity;
    }
}

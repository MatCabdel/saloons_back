package com.backend_project_template.domains.admin.stats;

import java.util.List;

/**
 * DTO pour les stats géographiques et pics d'activité (sous-menu "Villes &
 * Pics").
 */
public class GeographyStatsDTO {
    private List<RankedItem> entriesByCity;
    private List<HeatmapPoint> activityHeatmap;
    private List<RankedItem> peakHoursByCity;
    private long totalCities;

    public GeographyStatsDTO() {
    }

    public List<RankedItem> getEntriesByCity() {
        return entriesByCity;
    }

    public void setEntriesByCity(List<RankedItem> entriesByCity) {
        this.entriesByCity = entriesByCity;
    }

    public List<HeatmapPoint> getActivityHeatmap() {
        return activityHeatmap;
    }

    public void setActivityHeatmap(List<HeatmapPoint> activityHeatmap) {
        this.activityHeatmap = activityHeatmap;
    }

    public List<RankedItem> getPeakHoursByCity() {
        return peakHoursByCity;
    }

    public void setPeakHoursByCity(List<RankedItem> peakHoursByCity) {
        this.peakHoursByCity = peakHoursByCity;
    }

    public long getTotalCities() {
        return totalCities;
    }

    public void setTotalCities(long totalCities) {
        this.totalCities = totalCities;
    }
}

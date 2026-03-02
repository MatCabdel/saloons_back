package com.backend_project_template.domains.admin.stats;

import java.util.List;

/**
 * DTO pour les stats d'engagement saloons (sous-menu "Saloons & Engagement").
 */
public class SaloonEngagementStatsDTO {
    private List<TimeSeriesPoint> entriesPerDay;
    private long totalEntries;
    private long uniqueVisitors;
    private Double avgSaloonsPerUser;
    private List<RankedItem> topSaloons;
    private List<RankedItem> uniqueVisitorsPerSaloon;
    private List<RankedItem> entriesByCity;

    public SaloonEngagementStatsDTO() {
    }

    public List<TimeSeriesPoint> getEntriesPerDay() {
        return entriesPerDay;
    }

    public void setEntriesPerDay(List<TimeSeriesPoint> entriesPerDay) {
        this.entriesPerDay = entriesPerDay;
    }

    public long getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(long totalEntries) {
        this.totalEntries = totalEntries;
    }

    public long getUniqueVisitors() {
        return uniqueVisitors;
    }

    public void setUniqueVisitors(long uniqueVisitors) {
        this.uniqueVisitors = uniqueVisitors;
    }

    public Double getAvgSaloonsPerUser() {
        return avgSaloonsPerUser;
    }

    public void setAvgSaloonsPerUser(Double avgSaloonsPerUser) {
        this.avgSaloonsPerUser = avgSaloonsPerUser;
    }

    public List<RankedItem> getTopSaloons() {
        return topSaloons;
    }

    public void setTopSaloons(List<RankedItem> topSaloons) {
        this.topSaloons = topSaloons;
    }

    public List<RankedItem> getUniqueVisitorsPerSaloon() {
        return uniqueVisitorsPerSaloon;
    }

    public void setUniqueVisitorsPerSaloon(List<RankedItem> uniqueVisitorsPerSaloon) {
        this.uniqueVisitorsPerSaloon = uniqueVisitorsPerSaloon;
    }

    public List<RankedItem> getEntriesByCity() {
        return entriesByCity;
    }

    public void setEntriesByCity(List<RankedItem> entriesByCity) {
        this.entriesByCity = entriesByCity;
    }
}

package com.backend_project_template.domains.admin.stats;

/**
 * Point de données pour la heatmap jour x heure.
 * dayOfWeek: 1=Lundi ... 7=Dimanche
 * hour: 0-23
 * value: nombre d'entrées
 */
public class HeatmapPoint {
    private int dayOfWeek;
    private int hour;
    private long value;

    public HeatmapPoint() {
    }

    public HeatmapPoint(int dayOfWeek, int hour, long value) {
        this.dayOfWeek = dayOfWeek;
        this.hour = hour;
        this.value = value;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}

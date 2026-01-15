package com.backend_project_template.domains.admin;

public class PremiumMonthlyStatsDTO {
    private int year;
    private int month;
    private String monthName;
    private long activeSubscriptions;

    public PremiumMonthlyStatsDTO() {
    }

    public PremiumMonthlyStatsDTO(int year, int month, String monthName, long activeSubscriptions) {
        this.year = year;
        this.month = month;
        this.monthName = monthName;
        this.activeSubscriptions = activeSubscriptions;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public long getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(long activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }
}

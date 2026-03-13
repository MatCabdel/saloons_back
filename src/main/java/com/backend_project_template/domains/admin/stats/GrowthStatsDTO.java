package com.backend_project_template.domains.admin.stats;

import java.util.List;

/**
 * DTO pour les stats de croissance (sous-menu "Croissance").
 */
public class GrowthStatsDTO {
    private List<TimeSeriesPoint> newUsersPerDay;
    private long totalNewUsers;
    private double retentionD1;
    private double retentionD7;
    private double retentionD30;
    private long churnedUsers;
    private double churnRate;
    private long dau;
    private long wau;
    private long mau;

    public GrowthStatsDTO() {
    }

    public List<TimeSeriesPoint> getNewUsersPerDay() {
        return newUsersPerDay;
    }

    public void setNewUsersPerDay(List<TimeSeriesPoint> newUsersPerDay) {
        this.newUsersPerDay = newUsersPerDay;
    }

    public long getTotalNewUsers() {
        return totalNewUsers;
    }

    public void setTotalNewUsers(long totalNewUsers) {
        this.totalNewUsers = totalNewUsers;
    }

    public double getRetentionD1() {
        return retentionD1;
    }

    public void setRetentionD1(double retentionD1) {
        this.retentionD1 = retentionD1;
    }

    public double getRetentionD7() {
        return retentionD7;
    }

    public void setRetentionD7(double retentionD7) {
        this.retentionD7 = retentionD7;
    }

    public double getRetentionD30() {
        return retentionD30;
    }

    public void setRetentionD30(double retentionD30) {
        this.retentionD30 = retentionD30;
    }

    public long getChurnedUsers() {
        return churnedUsers;
    }

    public void setChurnedUsers(long churnedUsers) {
        this.churnedUsers = churnedUsers;
    }

    public double getChurnRate() {
        return churnRate;
    }

    public void setChurnRate(double churnRate) {
        this.churnRate = churnRate;
    }

    public long getDau() {
        return dau;
    }

    public void setDau(long dau) {
        this.dau = dau;
    }

    public long getWau() {
        return wau;
    }

    public void setWau(long wau) {
        this.wau = wau;
    }

    public long getMau() {
        return mau;
    }

    public void setMau(long mau) {
        this.mau = mau;
    }
}

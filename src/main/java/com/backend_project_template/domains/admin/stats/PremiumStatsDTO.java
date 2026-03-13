package com.backend_project_template.domains.admin.stats;

import java.util.List;

/**
 * DTO pour les stats Premium (sous-menu "Premium").
 */
public class PremiumStatsDTO {
    private long totalPremium;
    private long newSubscriptions;
    private double conversionRate;
    private List<RankedItem> premiumByCity;
    private List<TimeSeriesPoint> subscriptionsPerDay;

    public PremiumStatsDTO() {
    }

    public long getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(long totalPremium) {
        this.totalPremium = totalPremium;
    }

    public long getNewSubscriptions() {
        return newSubscriptions;
    }

    public void setNewSubscriptions(long newSubscriptions) {
        this.newSubscriptions = newSubscriptions;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public List<RankedItem> getPremiumByCity() {
        return premiumByCity;
    }

    public void setPremiumByCity(List<RankedItem> premiumByCity) {
        this.premiumByCity = premiumByCity;
    }

    public List<TimeSeriesPoint> getSubscriptionsPerDay() {
        return subscriptionsPerDay;
    }

    public void setSubscriptionsPerDay(List<TimeSeriesPoint> subscriptionsPerDay) {
        this.subscriptionsPerDay = subscriptionsPerDay;
    }
}

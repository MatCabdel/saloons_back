package com.backend_project_template.domains.admin.stats;

/**
 * DTO pour les stats de funnel.
 */
public class FunnelStatsDTO {
    private long totalRegistered;
    private long profileCompleted;
    private long enteredSaloon;
    private long matched;
    private long conversationStarted;
    private long heartRequestSent;
    private long returnedD1;
    private double pctNeverEnteredSaloon;
    private double pctEnteredNoMatch;

    public FunnelStatsDTO() {
    }

    public long getTotalRegistered() {
        return totalRegistered;
    }

    public void setTotalRegistered(long totalRegistered) {
        this.totalRegistered = totalRegistered;
    }

    public long getProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(long profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public long getEnteredSaloon() {
        return enteredSaloon;
    }

    public void setEnteredSaloon(long enteredSaloon) {
        this.enteredSaloon = enteredSaloon;
    }

    public long getMatched() {
        return matched;
    }

    public void setMatched(long matched) {
        this.matched = matched;
    }

    public long getConversationStarted() {
        return conversationStarted;
    }

    public void setConversationStarted(long conversationStarted) {
        this.conversationStarted = conversationStarted;
    }

    public long getHeartRequestSent() {
        return heartRequestSent;
    }

    public void setHeartRequestSent(long heartRequestSent) {
        this.heartRequestSent = heartRequestSent;
    }

    public long getReturnedD1() {
        return returnedD1;
    }

    public void setReturnedD1(long returnedD1) {
        this.returnedD1 = returnedD1;
    }

    public double getPctNeverEnteredSaloon() {
        return pctNeverEnteredSaloon;
    }

    public void setPctNeverEnteredSaloon(double pctNeverEnteredSaloon) {
        this.pctNeverEnteredSaloon = pctNeverEnteredSaloon;
    }

    public double getPctEnteredNoMatch() {
        return pctEnteredNoMatch;
    }

    public void setPctEnteredNoMatch(double pctEnteredNoMatch) {
        this.pctEnteredNoMatch = pctEnteredNoMatch;
    }
}

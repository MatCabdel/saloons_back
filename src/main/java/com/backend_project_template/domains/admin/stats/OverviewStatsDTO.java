package com.backend_project_template.domains.admin.stats;

/**
 * DTO pour la vue d'ensemble enrichie.
 */
public class OverviewStatsDTO {
    private long totalUsers;
    private long activeUsers;
    private long premiumUsers;
    private long totalSaloons;
    private int connectedUsers;
    private long totalMatches;
    private long totalConversations;
    private long totalMessages;
    private long citiesCovered;
    private long profilesCompleted;

    public OverviewStatsDTO() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getPremiumUsers() {
        return premiumUsers;
    }

    public void setPremiumUsers(long premiumUsers) {
        this.premiumUsers = premiumUsers;
    }

    public long getTotalSaloons() {
        return totalSaloons;
    }

    public void setTotalSaloons(long totalSaloons) {
        this.totalSaloons = totalSaloons;
    }

    public int getConnectedUsers() {
        return connectedUsers;
    }

    public void setConnectedUsers(int connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    public long getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(long totalMatches) {
        this.totalMatches = totalMatches;
    }

    public long getTotalConversations() {
        return totalConversations;
    }

    public void setTotalConversations(long totalConversations) {
        this.totalConversations = totalConversations;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public long getCitiesCovered() {
        return citiesCovered;
    }

    public void setCitiesCovered(long citiesCovered) {
        this.citiesCovered = citiesCovered;
    }

    public long getProfilesCompleted() {
        return profilesCompleted;
    }

    public void setProfilesCompleted(long profilesCompleted) {
        this.profilesCompleted = profilesCompleted;
    }
}

package com.backend_project_template.domains.admin.stats;

import java.util.List;

/**
 * DTO pour les stats Match & Chat (sous-menu "Match & Chat").
 */
public class MatchChatStatsDTO {
    private List<TimeSeriesPoint> matchesPerDay;
    private long totalMatches;
    private long conversationsStarted;
    private long totalMessages;
    private Double avgMessagesPerConversation;
    private long heartRequestsSent;
    private double matchRatePerEntry;
    private List<TimeSeriesPoint> heartRequestsPerDay;

    public MatchChatStatsDTO() {
    }

    public List<TimeSeriesPoint> getMatchesPerDay() {
        return matchesPerDay;
    }

    public void setMatchesPerDay(List<TimeSeriesPoint> matchesPerDay) {
        this.matchesPerDay = matchesPerDay;
    }

    public long getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(long totalMatches) {
        this.totalMatches = totalMatches;
    }

    public long getConversationsStarted() {
        return conversationsStarted;
    }

    public void setConversationsStarted(long conversationsStarted) {
        this.conversationsStarted = conversationsStarted;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public Double getAvgMessagesPerConversation() {
        return avgMessagesPerConversation;
    }

    public void setAvgMessagesPerConversation(Double avgMessagesPerConversation) {
        this.avgMessagesPerConversation = avgMessagesPerConversation;
    }

    public long getHeartRequestsSent() {
        return heartRequestsSent;
    }

    public void setHeartRequestsSent(long heartRequestsSent) {
        this.heartRequestsSent = heartRequestsSent;
    }

    public double getMatchRatePerEntry() {
        return matchRatePerEntry;
    }

    public void setMatchRatePerEntry(double matchRatePerEntry) {
        this.matchRatePerEntry = matchRatePerEntry;
    }

    public List<TimeSeriesPoint> getHeartRequestsPerDay() {
        return heartRequestsPerDay;
    }

    public void setHeartRequestsPerDay(List<TimeSeriesPoint> heartRequestsPerDay) {
        this.heartRequestsPerDay = heartRequestsPerDay;
    }
}

package com.backend_project_template.domains.admin;

import java.util.Map;

public class DashboardStatsDTO {

    private long totalUsers;
    private long activeUsers;
    private long premiumUsers;
    private long totalSaloons;
    private int connectedUsers;
    private Map<String, Long> saloonsByCity;

    public DashboardStatsDTO() {
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public DashboardStatsDTO(long totalUsers, long activeUsers, long premiumUsers, long totalSaloons,
            int connectedUsers, Map<String, Long> saloonsByCity) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.premiumUsers = premiumUsers;
        this.totalSaloons = totalSaloons;
        this.connectedUsers = connectedUsers;
        this.saloonsByCity = saloonsByCity;
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

    public Map<String, Long> getSaloonsByCity() {
        return saloonsByCity;
    }

    public void setSaloonsByCity(Map<String, Long> saloonsByCity) {
        this.saloonsByCity = saloonsByCity;
    }
}

package com.backend_project_template.domains.presence.dto;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse lors du join d'un saloon.
 */
@SuppressWarnings("checkstyle:ParameterNumber")
public class JoinResponseDTO {
    private Long saloonId;
    private String saloonName;
    private LocalDateTime joinedAt;
    private LocalDateTime endsAt;
    private int connectedUsersCount;
    private long remainingSeconds;

    public JoinResponseDTO() {
    }

    public JoinResponseDTO(Long saloonId, String saloonName, LocalDateTime joinedAt,
            LocalDateTime endsAt, int connectedUsersCount) {
        this.saloonId = saloonId;
        this.saloonName = saloonName;
        this.joinedAt = joinedAt;
        this.endsAt = endsAt;
        this.connectedUsersCount = connectedUsersCount;
        this.remainingSeconds = java.time.Duration.between(LocalDateTime.now(), endsAt).getSeconds();
    }

    public Long getSaloonId() {
        return saloonId;
    }

    public void setSaloonId(Long saloonId) {
        this.saloonId = saloonId;
    }

    public String getSaloonName() {
        return saloonName;
    }

    public void setSaloonName(String saloonName) {
        this.saloonName = saloonName;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public int getConnectedUsersCount() {
        return connectedUsersCount;
    }

    public void setConnectedUsersCount(int connectedUsersCount) {
        this.connectedUsersCount = connectedUsersCount;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}

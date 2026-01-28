package com.backend_project_template.domains.presence.dto;

import java.time.LocalDateTime;

/**
 * DTO pour la session active d'un utilisateur.
 */
@SuppressWarnings("checkstyle:ParameterNumber")
public class ActiveSessionDTO {
    private Long userId;
    private Long saloonId;
    private String saloonName;
    private LocalDateTime joinedAt;
    private LocalDateTime endsAt;
    private long remainingSeconds;
    private boolean isActive;

    public ActiveSessionDTO() {
    }

    public ActiveSessionDTO(Long userId, Long saloonId, String saloonName,
            LocalDateTime joinedAt, LocalDateTime endsAt) {
        this.userId = userId;
        this.saloonId = saloonId;
        this.saloonName = saloonName;
        this.joinedAt = joinedAt;
        this.endsAt = endsAt;
        this.isActive = LocalDateTime.now().isBefore(endsAt);
        this.remainingSeconds = isActive ? java.time.Duration.between(LocalDateTime.now(), endsAt).getSeconds() : 0;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}

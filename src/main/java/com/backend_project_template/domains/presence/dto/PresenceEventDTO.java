package com.backend_project_template.domains.presence.dto;

import java.time.LocalDateTime;

/**
 * DTO pour les événements WebSocket de présence.
 */
@SuppressWarnings("checkstyle:ParameterNumber")
public class PresenceEventDTO {
    private String type; // USER_JOINED, USER_LEFT
    private Long saloonId;
    private UserPresenceDTO user;
    private Long userId; // Pour USER_LEFT
    private int connectedCount;
    private LocalDateTime timestamp;

    public PresenceEventDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public PresenceEventDTO(String type, Long saloonId, UserPresenceDTO user, int connectedCount) {
        this();
        this.type = type;
        this.saloonId = saloonId;
        this.user = user;
        this.connectedCount = connectedCount;
    }

    public PresenceEventDTO(String type, Long saloonId, Long userId, int connectedCount) {
        this();
        this.type = type;
        this.saloonId = saloonId;
        this.userId = userId;
        this.connectedCount = connectedCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSaloonId() {
        return saloonId;
    }

    public void setSaloonId(Long saloonId) {
        this.saloonId = saloonId;
    }

    public UserPresenceDTO getUser() {
        return user;
    }

    public void setUser(UserPresenceDTO user) {
        this.user = user;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getConnectedCount() {
        return connectedCount;
    }

    public void setConnectedCount(int connectedCount) {
        this.connectedCount = connectedCount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

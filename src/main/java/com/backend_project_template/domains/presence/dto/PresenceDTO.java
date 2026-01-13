package com.backend_project_template.domains.presence.dto;

import java.util.List;

/**
 * DTO pour les informations de présence d'un saloon.
 */
@SuppressWarnings("checkstyle:ParameterNumber")
public class PresenceDTO {
    private Long saloonId;
    private String saloonName;
    private int connectedCount;
    private List<UserPresenceDTO> connectedUsers;

    public PresenceDTO() {
    }

    public PresenceDTO(Long saloonId, String saloonName, int connectedCount,
            List<UserPresenceDTO> connectedUsers) {
        this.saloonId = saloonId;
        this.saloonName = saloonName;
        this.connectedCount = connectedCount;
        this.connectedUsers = connectedUsers;
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

    public int getConnectedCount() {
        return connectedCount;
    }

    public void setConnectedCount(int connectedCount) {
        this.connectedCount = connectedCount;
    }

    public List<UserPresenceDTO> getConnectedUsers() {
        return connectedUsers;
    }

    public void setConnectedUsers(List<UserPresenceDTO> connectedUsers) {
        this.connectedUsers = connectedUsers;
    }
}

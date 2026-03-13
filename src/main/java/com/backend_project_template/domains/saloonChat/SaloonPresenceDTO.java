package com.backend_project_template.domains.saloonChat;

public class SaloonPresenceDTO {
    private Long saloonId;
    private int connectedCount;
    private boolean chatEnabled;

    public SaloonPresenceDTO(Long saloonId, int connectedCount, boolean chatEnabled) {
        this.saloonId = saloonId;
        this.connectedCount = connectedCount;
        this.chatEnabled = chatEnabled;
    }

    public Long getSaloonId() {
        return saloonId;
    }

    public int getConnectedCount() {
        return connectedCount;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }
}

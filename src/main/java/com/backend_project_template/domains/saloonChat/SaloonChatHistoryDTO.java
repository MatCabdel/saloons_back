package com.backend_project_template.domains.saloonChat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour retourner l'historique du chat avec le contexte de la session
 * utilisateur
 */
public class SaloonChatHistoryDTO {

    private List<SaloonMessageDTO> messages;
    private LocalDateTime joinedAt;
    private LocalDateTime sessionEndsAt;
    private boolean chatEnabled;
    private int connectedCount;

    public SaloonChatHistoryDTO() {
    }

    public SaloonChatHistoryDTO(List<SaloonMessageDTO> messages, LocalDateTime joinedAt,
            LocalDateTime sessionEndsAt, boolean chatEnabled, int connectedCount) {
        this.messages = messages;
        this.joinedAt = joinedAt;
        this.sessionEndsAt = sessionEndsAt;
        this.chatEnabled = chatEnabled;
        this.connectedCount = connectedCount;
    }

    public List<SaloonMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<SaloonMessageDTO> messages) {
        this.messages = messages;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getSessionEndsAt() {
        return sessionEndsAt;
    }

    public void setSessionEndsAt(LocalDateTime sessionEndsAt) {
        this.sessionEndsAt = sessionEndsAt;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    public int getConnectedCount() {
        return connectedCount;
    }

    public void setConnectedCount(int connectedCount) {
        this.connectedCount = connectedCount;
    }
}

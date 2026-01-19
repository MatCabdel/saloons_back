package com.backend_project_template.domains.saloonChat;

import java.time.LocalDateTime;

public class SaloonMessageDTO {
    private Long id;
    private Long saloonId;
    private Long senderId;
    private String senderName;
    private String senderImg;
    private String content;
    private LocalDateTime createdAt;

    public SaloonMessageDTO() {
    }

    public SaloonMessageDTO(SaloonMessage message) {
        this.id = message.getId();
        this.saloonId = message.getSaloon().getId();
        this.senderId = message.getSender().getId();
        this.senderName = message.getSender().getUserName();
        this.senderImg = message.getSender().getImgUrl();
        this.content = message.getContent();
        this.createdAt = message.getCreatedAt();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSaloonId() {
        return saloonId;
    }

    public void setSaloonId(Long saloonId) {
        this.saloonId = saloonId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderImg() {
        return senderImg;
    }

    public void setSenderImg(String senderImg) {
        this.senderImg = senderImg;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

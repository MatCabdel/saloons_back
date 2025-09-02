package com.backend_project_template.domains.message;

import java.time.LocalDateTime;

public class MessageDTO {

  private Long id;
  private Long conversationId;
  private Long senderId;
  private String senderName;
  private String content;
  private LocalDateTime sentAt;

  public MessageDTO() {}

  public MessageDTO(Message message) {
    this.id = message.getId();
    this.conversationId = message.getConversation() != null ? message.getConversation().getId() : null;
    this.senderId = message.getSender() != null ? message.getSender().getId() : null;
    this.senderName = message.getSender() != null ? message.getSender().getUserName() : null;
    this.content = message.getContent();
    this.sentAt = message.getSentAt();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getConversationId() {
    return conversationId;
  }

  public void setConversationId(Long conversationId) {
    this.conversationId = conversationId;
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

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public LocalDateTime getSentAt() {
    return sentAt;
  }

  public void setSentAt(LocalDateTime sentAt) {
    this.sentAt = sentAt;
  }
}

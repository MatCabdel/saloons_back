package com.backend_project_template.domains.conversation;

import com.backend_project_template.domains.message.MessageDTO;
import com.backend_project_template.domains.user.UserDTO;
import java.util.List;

public class ConversationDTO {

  private Long id;
  private List<UserDTO> participants;
  private MessageDTO lastMessage;

  public ConversationDTO() {}

  public ConversationDTO(Conversation conversation) {
    this.id = conversation.getId();
    this.participants = conversation.getParticipants().stream().map(UserDTO::new).toList();
    if (conversation.getMessages() != null && !conversation.getMessages().isEmpty()) {
      this.lastMessage = conversation
        .getMessages()
        .stream()
        .max((m1, m2) -> m1.getSentAt().compareTo(m2.getSentAt()))
        .map(MessageDTO::new)
        .orElse(null);
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public List<UserDTO> getParticipants() {
    return participants;
  }

  public void setParticipants(List<UserDTO> participants) {
    this.participants = participants;
  }

  public MessageDTO getLastMessage() {
    return lastMessage;
  }

  public void setLastMessage(MessageDTO lastMessage) {
    this.lastMessage = lastMessage;
  }
}

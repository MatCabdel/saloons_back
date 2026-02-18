package com.backend_project_template.domains.conversation;

import com.backend_project_template.domains.message.MessageDTO;
import com.backend_project_template.domains.user.UserDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class ConversationDTO {

  private Long id;
  private List<UserDTO> participants;
  private MessageDTO lastMessage;
  private boolean otherParticipantLeft;
  @JsonProperty("isPermanent")
  private boolean isPermanent;
  private LocalDateTime expiredAt;
  @JsonProperty("isMatchCancelled")
  private boolean isMatchCancelled; // true si match annulé, false si juste quitté le saloon
  private int unreadCount; // Nombre de messages non lus pour l'utilisateur courant

  public ConversationDTO() {
  }

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
    this.otherParticipantLeft = false;
    this.isPermanent = conversation.isPermanent();
    this.expiredAt = null;
    this.isMatchCancelled = false;
  }

  public ConversationDTO(Conversation conversation, Long currentUserId) {
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
    // Vérifier si N'IMPORTE QUEL participant a quitté (conversation expirée)
    boolean anyParticipantLeft = conversation.getConversationParticipants().stream()
        .anyMatch(ConversationParticipant::hasLeft);
    this.otherParticipantLeft = anyParticipantLeft;
    this.isPermanent = conversation.isPermanent();
    // Récupérer la date d'expiration (le premier leftAt trouvé)
    this.expiredAt = conversation.getConversationParticipants().stream()
        .filter(ConversationParticipant::hasLeft)
        .map(ConversationParticipant::getLeftAt)
        .findFirst()
        .orElse(null);
    // isMatchCancelled sera set par le service qui a accès au MatchRepository
    this.isMatchCancelled = false;
    
    // Calculer le nombre de messages non lus pour l'utilisateur courant
    this.unreadCount = calculateUnreadCount(conversation, currentUserId);
  }

  /**
   * Calcule le nombre de messages non lus pour un utilisateur donné.
   * Compte les messages envoyés APRÈS le lastReadAt du participant.
   */
  private int calculateUnreadCount(Conversation conversation, Long currentUserId) {
    if (conversation.getMessages() == null || conversation.getMessages().isEmpty()) {
      return 0;
    }
    
    // Trouver le participant courant
    ConversationParticipant currentParticipant = conversation.getConversationParticipants().stream()
        .filter(cp -> cp.getUser().getId().equals(currentUserId))
        .findFirst()
        .orElse(null);
    
    if (currentParticipant == null) {
      return 0;
    }
    
    // Si lastReadAt est null, tous les messages non envoyés par moi sont non lus
    // Sinon, compter les messages après lastReadAt non envoyés par moi
    return (int) conversation.getMessages().stream()
        .filter(msg -> !msg.getSender().getId().equals(currentUserId)) // Pas mes messages
        .filter(msg -> {
          if (currentParticipant.getLastReadAt() == null) {
            return true; // Jamais lu = tous les messages des autres sont non lus
          }
          return msg.getSentAt().isAfter(currentParticipant.getLastReadAt());
        })
        .count();
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

  public boolean isOtherParticipantLeft() {
    return otherParticipantLeft;
  }

  public void setOtherParticipantLeft(boolean otherParticipantLeft) {
    this.otherParticipantLeft = otherParticipantLeft;
  }

  public boolean isPermanent() {
    return isPermanent;
  }

  public void setPermanent(boolean isPermanent) {
    this.isPermanent = isPermanent;
  }

  public LocalDateTime getExpiredAt() {
    return expiredAt;
  }

  public void setExpiredAt(LocalDateTime expiredAt) {
    this.expiredAt = expiredAt;
  }

  public boolean isMatchCancelled() {
    return isMatchCancelled;
  }

  public void setMatchCancelled(boolean isMatchCancelled) {
    this.isMatchCancelled = isMatchCancelled;
  }

  public int getUnreadCount() {
    return unreadCount;
  }

  public void setUnreadCount(int unreadCount) {
    this.unreadCount = unreadCount;
  }
}

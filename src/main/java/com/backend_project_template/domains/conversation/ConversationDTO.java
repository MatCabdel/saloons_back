package com.backend_project_template.domains.conversation;

import com.backend_project_template.domains.message.MessageDTO;
import com.backend_project_template.domains.user.UserDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class ConversationDTO {

  /** Durée de la fenêtre coup de cœur en heures */
  private static final int HEART_REQUEST_WINDOW_HOURS = 12;

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
  @JsonProperty("isHeartWindowExpired")
  private boolean isHeartWindowExpired; // true si la fenêtre 12h pour envoyer un coup de cœur est expirée

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
    this.isHeartWindowExpired = false;
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
    this.isPermanent = conversation.isPermanent();
    
    // Vérifier si N'IMPORTE QUEL participant a quitté (conversation expirée)
    boolean anyParticipantLeft = conversation.getConversationParticipants().stream()
        .anyMatch(ConversationParticipant::hasLeft);
    
    // Récupérer la date d'expiration (le premier leftAt trouvé)
    LocalDateTime participantExpiredAt = conversation.getConversationParticipants().stream()
        .filter(ConversationParticipant::hasLeft)
        .map(ConversationParticipant::getLeftAt)
        .findFirst()
        .orElse(null);
    
    // FALLBACK pour les vieilles conversations sans leftAt:
    // Si le saloon est fermé et qu'aucun participant n'a quitté,
    // considérer la conversation comme expirée depuis la fermeture du saloon
    boolean saloonClosed = conversation.getSaloon() != null 
        && conversation.getSaloon().getClosedAt() != null;
    LocalDateTime saloonClosedAt = saloonClosed ? conversation.getSaloon().getClosedAt() : null;
    
    // La conversation est expirée si un participant a quitté OU si le saloon est fermé
    this.otherParticipantLeft = anyParticipantLeft || (saloonClosed && !this.isPermanent);
    
    // Date d'expiration: priorité au leftAt, sinon closedAt du saloon
    this.expiredAt = participantExpiredAt != null ? participantExpiredAt : saloonClosedAt;
    
    // isMatchCancelled sera set par le service qui a accès au MatchRepository
    this.isMatchCancelled = false;

    // Calculer le nombre de messages non lus pour l'utilisateur courant
    this.unreadCount = calculateUnreadCount(conversation, currentUserId);
    
    // Calculer si la fenêtre 12h est expirée
    this.isHeartWindowExpired = calculateHeartWindowExpired();
  }
  
  /**
   * Calcule si la fenêtre de 12h pour envoyer un coup de cœur est expirée.
   * La fenêtre est expirée si:
   * - La conversation n'est pas permanente ET
   * - Un participant a quitté (expiredAt non null) ET
   * - Plus de 12h se sont écoulées depuis expiredAt
   */
  private boolean calculateHeartWindowExpired() {
    if (isPermanent) {
      return false; // Conversation permanente, pas de fenêtre
    }
    if (expiredAt == null) {
      return false; // Conversation active, pas encore expirée
    }
    LocalDateTime windowEnd = expiredAt.plusHours(HEART_REQUEST_WINDOW_HOURS);
    return LocalDateTime.now().isAfter(windowEnd);
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

  public boolean isHeartWindowExpired() {
    return isHeartWindowExpired;
  }

  public void setHeartWindowExpired(boolean isHeartWindowExpired) {
    this.isHeartWindowExpired = isHeartWindowExpired;
  }
}

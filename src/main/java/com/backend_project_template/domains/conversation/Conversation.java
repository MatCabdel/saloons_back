package com.backend_project_template.domains.conversation;

import com.backend_project_template.domains.message.Message;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.user.User;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Conversation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Le saloon dans lequel la conversation a été créée.
   */
  @ManyToOne
  @JoinColumn(name = "saloon_id")
  private Saloon saloon;

  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ConversationParticipant> conversationParticipants = new ArrayList<>();

  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
  private List<Message> messages;

  /**
   * Indique si la conversation est permanente (suite à des coups de cœur
   * mutuels).
   */
  @Column(name = "is_permanent", nullable = false)
  @Builder.Default
  private boolean isPermanent = false;

  /**
   * Retourne les participants actifs (qui n'ont pas quitté).
   */
  public List<User> getActiveParticipants() {
    return conversationParticipants.stream()
        .filter(cp -> !cp.hasLeft())
        .map(ConversationParticipant::getUser)
        .collect(Collectors.toList());
  }

  /**
   * Retourne tous les participants (actifs ou non) - sans doublons.
   */
  public List<User> getParticipants() {
    return conversationParticipants.stream()
        .map(ConversationParticipant::getUser)
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Vérifie si un participant a quitté la conversation.
   */
  public boolean hasParticipantLeft(Long userId) {
    return conversationParticipants.stream()
        .anyMatch(cp -> cp.getUser().getId().equals(userId) && cp.hasLeft());
  }

  /**
   * Retourne le ConversationParticipant pour un utilisateur donné.
   */
  public ConversationParticipant getParticipant(Long userId) {
    return conversationParticipants.stream()
        .filter(cp -> cp.getUser().getId().equals(userId))
        .findFirst()
        .orElse(null);
  }
}

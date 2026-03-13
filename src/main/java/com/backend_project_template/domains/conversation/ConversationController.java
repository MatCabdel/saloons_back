package com.backend_project_template.domains.conversation;

import com.backend_project_template.domains.heartRequest.HeartRequestRepository;
import com.backend_project_template.domains.match.MatchService;
import com.backend_project_template.domains.message.MessageDTO;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conversations")
public class ConversationController {
  private static final int HEART_REQUEST_WINDOW_HOURS = 24;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ConversationRepository conversationRepository;

  @Autowired
  private ConversationParticipantRepository participantRepository;

  @Autowired
  private SaloonRepository saloonRepository;

  @Autowired
  private HeartRequestRepository heartRequestRepository;

  @Autowired
  private MatchService matchService;

  @GetMapping
  public Map<String, List<ConversationDTO>> getUserConversations(Principal principal) {
    if (principal == null) {
      throw new RuntimeException("Not authenticated");
    }
    User user = userRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));
    List<ConversationDTO> conversations = conversationRepository.findAllConversationsForUser(user).stream()
        // Garder les conversations même si l'utilisateur courant a quitté,
        // tant que la fenêtre "coup de cœur" (12h) est encore active.
        .filter(conv -> {
          ConversationParticipant myParticipant = conv.getParticipant(user.getId());
          if (myParticipant == null)
            return false;
          User otherUser = conv.getParticipants().stream()
              .filter(u -> !u.getId().equals(user.getId()))
              .findFirst()
              .orElse(null);
          // Si l'utilisateur courant a explicitement quitté/supprimé le match,
          // on masque la conversation de SA liste.
          if (otherUser != null && matchService.hasUserLeft(user, otherUser)) {
            return false;
          }
          if (conv.isPermanent() || myParticipant.getLeftAt() == null) {
            return true;
          }
          LocalDateTime heartWindowEnd = myParticipant.getLeftAt().plusHours(HEART_REQUEST_WINDOW_HOURS);
          return LocalDateTime.now().isBefore(heartWindowEnd);
        })
        .map(conv -> {
          ConversationDTO dto = new ConversationDTO(conv, user.getId());
          // Vérifier si le match est annulé (l'AUTRE utilisateur a quitté le match)
          User otherUser = conv.getParticipants().stream()
              .filter(u -> !u.getId().equals(user.getId()))
              .findFirst()
              .orElse(null);
          if (otherUser != null) {
            // hasOtherUserLeft = l'autre a quitté MOI
            // On ne met matchCancelled QUE si l'autre a quitté, pas si c'est moi
            boolean otherUserLeftMatch = matchService.hasOtherUserLeft(user, otherUser);
            dto.setMatchCancelled(otherUserLeftMatch);
          }
          return dto;
        })
        .toList();
    return Map.of("payload", conversations);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ConversationDTO> getConversation(@PathVariable Long id, Principal principal) {
    User currentUser = userRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));
    Conversation conversation = conversationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Conversation not found"));

    // Vérifier que l'utilisateur fait partie de la conversation
    ConversationParticipant participant = conversation.getParticipant(currentUser.getId());
    if (participant == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Créer le DTO et vérifier si le match est annulé
    ConversationDTO dto = new ConversationDTO(conversation, currentUser.getId());
    User otherUser = conversation.getParticipants().stream()
        .filter(u -> !u.getId().equals(currentUser.getId()))
        .findFirst()
        .orElse(null);
    if (otherUser != null) {
      // Même règle que la liste: "match annulé" seulement si l'AUTRE utilisateur a
      // quitté le match.
      boolean matchCancelled = matchService.hasOtherUserLeft(currentUser, otherUser);
      dto.setMatchCancelled(matchCancelled);
    }

    // Permettre l'accès dans tous les cas (expirée, annulée, etc.)
    // Le frontend gèrera l'affichage approprié
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/{id}/messages")
  public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long id, Principal principal) {
    User currentUser = userRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));
    Conversation conversation = conversationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Conversation not found"));

    // Vérifier que l'utilisateur fait partie de la conversation
    ConversationParticipant participant = conversation.getParticipant(currentUser.getId());
    if (participant == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Permettre l'accès aux messages même si l'utilisateur a quitté (pour les coups
    // de cœur)
    return ResponseEntity.ok(conversation.getMessages().stream().map(MessageDTO::new).toList());
  }

  @PostMapping
  public ResponseEntity<?> createConversation(@RequestBody CreateConversationDTO request, Principal principal) {
    User currentUser = userRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));
    User otherUser = userRepository.findById(request.participantId())
        .orElseThrow(() -> new RuntimeException("Participant not found"));

    // Sécurité : empêcher de créer une conversation avec soi-même
    if (currentUser.getId().equals(otherUser.getId())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Vous ne pouvez pas créer une conversation avec vous-même");
    }

    // Sécurité : vérifier que les utilisateurs sont matchés
    if (!matchService.isMatched(currentUser, otherUser)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous devez avoir un match avec cet utilisateur");
    }

    // Récupérer le saloon si fourni
    Saloon saloon = null;
    if (request.saloonId() != null) {
      saloon = saloonRepository.findById(request.saloonId()).orElse(null);
    }

    final Saloon finalSaloon = saloon;

    // Vérifier si une conversation existe déjà entre ces deux utilisateurs
    ConversationDTO dto = conversationRepository.findConversationBetweenUsers(currentUser, otherUser)
        .map(existingConv -> {
          // Réactiver le participant s'il avait quitté
          ConversationParticipant cp = existingConv.getParticipant(currentUser.getId());
          if (cp != null && cp.hasLeft()) {
            cp.setLeftAt(null);
            cp.setJoinedAt(LocalDateTime.now());
            participantRepository.save(cp);
          }
          // Mettre à jour le saloon si pas déjà défini
          if (existingConv.getSaloon() == null && finalSaloon != null) {
            existingConv.setSaloon(finalSaloon);
            conversationRepository.save(existingConv);
          }
          return new ConversationDTO(existingConv, currentUser.getId());
        })
        .orElseGet(() -> {
          Conversation conversation = new Conversation();
          conversation.setConversationParticipants(new ArrayList<>());
          conversation.setSaloon(finalSaloon);
          conversation = conversationRepository.save(conversation);

          // Créer les participants
          ConversationParticipant cp1 = ConversationParticipant.builder()
              .conversation(conversation)
              .user(currentUser)
              .joinedAt(LocalDateTime.now())
              .build();
          ConversationParticipant cp2 = ConversationParticipant.builder()
              .conversation(conversation)
              .user(otherUser)
              .joinedAt(LocalDateTime.now())
              .build();

          participantRepository.save(cp1);
          participantRepository.save(cp2);

          conversation.getConversationParticipants().add(cp1);
          conversation.getConversationParticipants().add(cp2);

          return new ConversationDTO(conversation, currentUser.getId());
        });

    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteConversation(@PathVariable Long id, Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    User currentUser = userRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));

    Conversation conversation = conversationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Conversation not found"));

    // Vérifier que l'utilisateur fait partie de la conversation
    ConversationParticipant participant = conversation.getParticipant(currentUser.getId());
    if (participant == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Trouver l'autre utilisateur pour aussi quitter le match
    User otherUser = conversation.getParticipants().stream()
        .filter(u -> !u.getId().equals(currentUser.getId()))
        .findFirst()
        .orElse(null);

    // Soft delete conversation : marquer comme quitté
    participant.setLeftAt(LocalDateTime.now());
    participantRepository.save(participant);

    // Supprimer les HeartRequests de l'utilisateur pour cette conversation
    // (winks envoyés ou reçus par l'utilisateur courant)
    heartRequestRepository.findByConversationId(id).stream()
        .filter(hr -> hr.getSender().getId().equals(currentUser.getId())
            || hr.getReceiver().getId().equals(currentUser.getId()))
        .forEach(heartRequestRepository::delete);

    // Soft delete match aussi
    if (otherUser != null) {
      matchService.leaveMatch(currentUser, otherUser);
    }

    return ResponseEntity.noContent().build();
  }

  /**
   * Marque une conversation comme lue pour l'utilisateur courant.
   * Met à jour lastReadAt du participant.
   */
  @PostMapping("/{id}/mark-as-read")
  public ResponseEntity<Void> markAsRead(@PathVariable Long id, Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    User currentUser = userRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));

    Conversation conversation = conversationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Conversation not found"));

    // Vérifier que l'utilisateur fait partie de la conversation
    ConversationParticipant participant = conversation.getParticipant(currentUser.getId());
    if (participant == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Mettre à jour lastReadAt
    participant.markAsRead();
    participantRepository.save(participant);

    return ResponseEntity.ok().build();
  }
}

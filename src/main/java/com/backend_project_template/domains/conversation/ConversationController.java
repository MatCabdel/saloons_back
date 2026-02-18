package com.backend_project_template.domains.conversation;

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

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ConversationRepository conversationRepository;

  @Autowired
  private ConversationParticipantRepository participantRepository;

  @Autowired
  private SaloonRepository saloonRepository;

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
        .map(conv -> {
          ConversationDTO dto = new ConversationDTO(conv, user.getId());
          // Vérifier si le match est annulé (un des deux a quitté le match)
          User otherUser = conv.getParticipants().stream()
              .filter(u -> !u.getId().equals(user.getId()))
              .findFirst()
              .orElse(null);
          if (otherUser != null) {
            boolean matchCancelled = matchService.hasOtherUserLeft(user, otherUser) 
                || matchService.hasUserLeft(user, otherUser);
            dto.setMatchCancelled(matchCancelled);
          }
          return dto;
        })
        // Filtrer les conversations où le match a été annulé (sauf si permanente)
        .filter(dto -> !dto.isMatchCancelled() || dto.isPermanent())
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
      boolean matchCancelled = matchService.hasOtherUserLeft(currentUser, otherUser)
          || matchService.hasUserLeft(currentUser, otherUser);
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

    // Permettre l'accès aux messages même si l'utilisateur a quitté (pour les coups de cœur)
    return ResponseEntity.ok(conversation.getMessages().stream().map(MessageDTO::new).toList());
  }

  @PostMapping
  public ConversationDTO createConversation(@RequestBody CreateConversationDTO request, Principal principal) {
    User currentUser = userRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));
    User otherUser = userRepository.findById(request.participantId())
        .orElseThrow(() -> new RuntimeException("Participant not found"));

    // Récupérer le saloon si fourni
    Saloon saloon = null;
    if (request.saloonId() != null) {
      saloon = saloonRepository.findById(request.saloonId()).orElse(null);
    }

    final Saloon finalSaloon = saloon;

    // Vérifier si une conversation existe déjà entre ces deux utilisateurs
    return conversationRepository.findConversationBetweenUsers(currentUser, otherUser)
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

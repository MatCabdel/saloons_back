package com.backend_project_template.domains.conversation;

import com.backend_project_template.domains.message.MessageDTO;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ConversationRepository conversationRepository;

  @GetMapping
  public Map<String, List<ConversationDTO>> getUserConversations(Principal principal) {
    if (principal == null) {
      throw new RuntimeException("Not authenticated");
    }
    User user = userRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));
    List<ConversationDTO> conversations = conversationRepository.findByParticipantsContaining(user).stream()
        .map(ConversationDTO::new).toList();
    return Map.of("payload", conversations);
  }

  @GetMapping("/{id}")
  public ConversationDTO getConversation(@PathVariable Long id) {
    Conversation conversation = conversationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Conversation not found"));
    return new ConversationDTO(conversation);
  }

  @GetMapping("/{id}/messages")
  public List<MessageDTO> getMessages(@PathVariable Long id) {
    Conversation conversation = conversationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Conversation not found"));
    return conversation.getMessages().stream().map(MessageDTO::new).toList();
  }

  @PostMapping
  public ConversationDTO createConversation(@RequestBody Long participantId, Principal principal) {
    User currentUser = userRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));
    User otherUser = userRepository.findById(participantId)
        .orElseThrow(() -> new RuntimeException("Participant not found"));

    // Vérifier si une conversation existe déjà entre ces deux utilisateurs
    return conversationRepository.findConversationBetweenUsers(currentUser, otherUser)
        .map(ConversationDTO::new)
        .orElseGet(() -> {
          Conversation conversation = new Conversation();
          conversation.setParticipants(List.of(currentUser, otherUser));
          conversation = conversationRepository.save(conversation);
          return new ConversationDTO(conversation);
        });
  }
}

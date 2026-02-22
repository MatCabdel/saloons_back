package com.backend_project_template.domains.message;

import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.conversation.ConversationRepository;
import com.backend_project_template.domains.pushtoken.FcmNotificationService;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Controller;

@Controller
public class ChatMessageController {

  private static final int MESSAGE_TRUNCATE_LENGTH = 50;
  private static final int ELLIPSIS_LENGTH = 3;

  @Autowired
  private SimpMessageSendingOperations messagingTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private ConversationRepository conversationRepository;

  @Autowired
  private FcmNotificationService fcmNotificationService;

  @MessageMapping("/chat.sendMessage")
  @SendTo("/topic/public")
  public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
    return chatMessage;
  }

  @MessageMapping("/chat.addUser")
  @SendTo("/topic/public")
  public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
    headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
    return chatMessage;
  }

  @MessageMapping("/chat.sendPrivateMessage")
  public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
    if (chatMessage.getConversation() == null || chatMessage.getConversation().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation ID is required");
    }
    if (chatMessage.getSender() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender is required");
    }

    Long senderId;
    try {
      senderId = Long.valueOf(chatMessage.getSender());
    } catch (NumberFormatException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sender format");
    }

    Conversation conversation = conversationRepository.findById(chatMessage.getConversation().getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));

    // Sécurité: empêcher tout envoi vers une conversation dont l'expéditeur
    // n'est pas participant.
    if (conversation.getParticipant(senderId) == null) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sender is not a participant of conversation");
    }

    Message message = new Message();
    message.setContent(chatMessage.getContent());
    message.setSentAt(chatMessage.getSentAt() != null ? chatMessage.getSentAt() : LocalDateTime.now());
    message.setConversation(conversation);
    message.setSender(sender);
    messageRepository.save(message);

    MessageDTO wsMessage = new MessageDTO(message);
    String destination = "/queue/conversation." + conversation.getId();
    messagingTemplate.convertAndSend(destination, wsMessage);

    // Envoyer une notification push aux autres participants
    sendPushNotificationToRecipients(conversation, sender, message);
  }

  /**
   * Envoie une notification push aux participants de la conversation (sauf l'expéditeur).
   */
  private void sendPushNotificationToRecipients(Conversation conversation, User sender, Message message) {
    // Récupérer les participants actifs sauf l'expéditeur
    List<Long> recipientIds = conversation.getActiveParticipants().stream()
        .filter(user -> !user.getId().equals(sender.getId()))
        .map(User::getId)
        .toList();

    if (recipientIds.isEmpty()) {
      return;
    }

    // Construire le contenu de la notification
    String senderName = sender.getFirstName() != null ? sender.getFirstName() : "Quelqu'un";
    String title = "Nouveau message";
    String body = senderName + " : " + truncateMessage(message.getContent(), MESSAGE_TRUNCATE_LENGTH);

    // Données additionnelles pour la navigation
    Map<String, String> data = new HashMap<>();
    data.put("type", "private_message");
    data.put("conversationId", String.valueOf(conversation.getId()));
    data.put("messageId", String.valueOf(message.getId()));
    data.put("senderId", String.valueOf(sender.getId()));

    // Envoyer la notification (asynchrone)
    fcmNotificationService.sendToUsers(recipientIds, title, body, data);
  }

  /**
   * Tronque un message à la longueur spécifiée.
   */
  private String truncateMessage(String content, int maxLength) {
    if (content == null) {
      return "";
    }
    if (content.length() <= maxLength) {
      return content;
    }
    return content.substring(0, maxLength - ELLIPSIS_LENGTH) + "...";
  }
}

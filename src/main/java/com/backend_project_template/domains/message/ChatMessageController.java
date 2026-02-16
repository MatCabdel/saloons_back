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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
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
    Message message = new Message();
    message.setContent(chatMessage.getContent());
    message.setSentAt(chatMessage.getSentAt() != null ? chatMessage.getSentAt() : LocalDateTime.now());
    Conversation conversation = conversationRepository.findById(chatMessage.getConversation().getId()).orElseThrow();
    message.setConversation(conversation);
    User sender = userRepository.findById(Long.valueOf(chatMessage.getSender())).orElseThrow();
    message.setSender(sender);
    messageRepository.save(message);
    String destination = "/queue/conversation." + conversation.getId();
    messagingTemplate.convertAndSend(destination, chatMessage);

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

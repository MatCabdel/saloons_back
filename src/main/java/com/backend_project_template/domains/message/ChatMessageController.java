package com.backend_project_template.domains.message;

import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.conversation.ConversationParticipant;
import com.backend_project_template.domains.conversation.ConversationRepository;
import com.backend_project_template.domains.pushtoken.FcmNotificationService;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Controller;

@Controller
public class ChatMessageController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChatMessageController.class);
  private static final int HEART_REQUEST_WINDOW_HOURS = 24;
  private static final int MESSAGE_MAX_LENGTH = 500;

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

  @MessageMapping("/chat.sendPrivateMessage")
  public void sendPrivateMessage(@Payload ChatMessage chatMessage, java.security.Principal principal) {
    if (chatMessage.getConversation() == null || chatMessage.getConversation().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation ID is required");
    }

    // Sécurité : utiliser le Principal authentifié (JWT) au lieu du sender du
    // payload
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }
    String authenticatedEmail = principal.getName();
    User sender = userRepository.findByEmail(authenticatedEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    Long senderId = sender.getId();

    // Garder findByIdWithParticipants pour éviter LazyInitializationException
    Conversation conversation = conversationRepository.findByIdWithParticipants(chatMessage.getConversation().getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

    // Sécurité: sender doit être participant de la conversation
    if (conversation.getParticipant(senderId) == null) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sender is not a participant of conversation");
    }

    // Règle métier: bloquer si conversation expirée + fenêtre fermée
    if (!canSendMessage(conversation)) {
      LOGGER.warn("📩 [message_blocked] conversationId={}, reason=conversation_expired_and_window_closed",
          conversation.getId());
      sendErrorToSender(String.valueOf(senderId), conversation.getId(),
          "Conversation expirée, vous ne pouvez plus envoyer de messages.");
      return;
    }

    Message message = new Message();
    String content = chatMessage.getContent();
    if (content == null || content.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content cannot be empty");
    }
    if (content.length() > MESSAGE_MAX_LENGTH) {
      content = content.substring(0, MESSAGE_MAX_LENGTH);
    }
    message.setContent(content);
    message.setSentAt(LocalDateTime.now());
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
   * Vérifie si l'envoi de message est autorisé dans cette conversation.
   * 
   * Règles:
   * - Si isPermanent = true → autorisé
   * - Si aucun participant n'a quitté (leftAt null) → autorisé (conversation
   * active)
   * - Si un participant a quitté ET on est dans la fenêtre 12h → autorisé
   * - Si un participant a quitté ET la fenêtre 12h est expirée → BLOQUÉ
   */
  private boolean canSendMessage(Conversation conversation) {
    // Conversation permanente = toujours OK
    if (conversation.isPermanent()) {
      LOGGER.debug("📩 [message_check] conversationId={}, result=allowed, reason=permanent", conversation.getId());
      return true;
    }

    // Trouver si un participant a quitté (leftAt non null)
    LocalDateTime expiredAt = conversation.getConversationParticipants().stream()
        .filter(cp -> cp.getLeftAt() != null)
        .map(ConversationParticipant::getLeftAt)
        .findFirst()
        .orElse(null);

    // Personne n'a quitté = conversation active
    if (expiredAt == null) {
      LOGGER.debug("📩 [message_check] conversationId={}, result=allowed, reason=active_conversation",
          conversation.getId());
      return true;
    }

    // Un participant a quitté, vérifier la fenêtre de temps
    LocalDateTime windowEnd = expiredAt.plusHours(HEART_REQUEST_WINDOW_HOURS);
    boolean withinWindow = LocalDateTime.now().isBefore(windowEnd);

    if (withinWindow) {
      LOGGER.debug(
          "📩 [message_check] conversationId={}, result=allowed, reason=within_window, expiredAt={}, windowEnd={}",
          conversation.getId(), expiredAt, windowEnd);
      return true;
    }

    // Fenêtre expirée = BLOQUÉ
    LOGGER.info(
        "📩 [message_check] conversationId={}, result=BLOCKED, reason=window_expired, expiredAt={}, windowEnd={}",
        conversation.getId(), expiredAt, windowEnd);
    return false;
  }

  /**
   * Envoie un message d'erreur au sender via WebSocket.
   */
  private void sendErrorToSender(String senderId, Long conversationId, String errorMessage) {
    Map<String, Object> errorPayload = new HashMap<>();
    errorPayload.put("type", "error");
    errorPayload.put("conversationId", conversationId);
    errorPayload.put("message", errorMessage);

    String destination = "/queue/user." + senderId + ".errors";
    messagingTemplate.convertAndSend(destination, errorPayload);
  }

  /**
   * Envoie une notification push aux participants de la conversation (sauf
   * l'expéditeur).
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
    // Format : Titre "Saloons", Body "X vous a envoyé un nouveau message !"
    String senderName = sender.getUserName() != null
        ? sender.getUserName()
        : (sender.getFirstName() != null ? sender.getFirstName() : "Quelqu'un");
    String title = "Saloons";
    String body = senderName + " vous a envoyé un nouveau message !";

    // Données additionnelles pour la navigation
    Map<String, String> data = new HashMap<>();
    data.put("type", "private_message");
    data.put("conversationId", String.valueOf(conversation.getId()));
    data.put("messageId", String.valueOf(message.getId()));
    data.put("senderId", String.valueOf(sender.getId()));

    // Envoyer la notification (asynchrone)
    fcmNotificationService.sendToUsers(recipientIds, title, body, data);
  }
}

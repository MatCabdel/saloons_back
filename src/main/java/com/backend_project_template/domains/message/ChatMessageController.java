package com.backend_project_template.domains.message;

import com.backend_project_template.Entity.User;
import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.conversation.ConversationRepository;
import com.backend_project_template.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class ChatMessageController {

  @Autowired
  private SimpMessageSendingOperations messagingTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private ConversationRepository conversationRepository;

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
  }
}

package com.backend_project_template.config;

import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.conversation.ConversationRepository;
import com.backend_project_template.domains.session.SessionRedisService;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import com.backend_project_template.security.JwtService;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * Intercepteur WebSocket STOMP qui authentifie les connexions via JWT
 * et contrôle les abonnements aux topics/queues.
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
  private static final String BEARER_PREFIX = "Bearer ";

  private static final Pattern CONVERSATION_QUEUE_PATTERN = Pattern.compile("^/queue/conversation\\.(\\d+)$");
  private static final Pattern SALOON_CHAT_PATTERN = Pattern.compile("^/topic/saloon-chat/(\\d+)$");
  private static final Pattern SALOON_PRESENCE_PATTERN = Pattern.compile("^/topic/saloon-presence/(\\d+)$");

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final UserRepository userRepository;
  private final ConversationRepository conversationRepository;
  private final SessionRedisService sessionRedisService;

  public WebSocketAuthInterceptor(JwtService jwtService, UserDetailsService userDetailsService,
      UserRepository userRepository, ConversationRepository conversationRepository,
      @Lazy SessionRedisService sessionRedisService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.userRepository = userRepository;
    this.conversationRepository = conversationRepository;
    this.sessionRedisService = sessionRedisService;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null) {
      return message;
    }

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      handleConnect(accessor);
    } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      handleSubscribe(accessor);
    }

    return message;
  }

  private void handleConnect(StompHeaderAccessor accessor) {
    // Extraire le token JWT du header STOMP "Authorization"
    List<String> authHeaders = accessor.getNativeHeader("Authorization");
    if (authHeaders == null || authHeaders.isEmpty()) {
      LOGGER.warn("🔒 [ws_auth] Connexion WebSocket refusée : pas de header Authorization");
      throw new org.springframework.messaging.MessageDeliveryException("Authentication required");
    }

    String authHeader = authHeaders.get(0);
    if (!authHeader.startsWith(BEARER_PREFIX)) {
      LOGGER.warn("🔒 [ws_auth] Connexion WebSocket refusée : format Bearer invalide");
      throw new org.springframework.messaging.MessageDeliveryException("Invalid authorization format");
    }

    String jwt = authHeader.substring(BEARER_PREFIX.length());
    try {
      String username = jwtService.extractClaims(jwt).getSubject();
      if (username != null && jwtService.extractClaims(jwt).getExpiration().after(new Date())) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        accessor.setUser(authentication);
        LOGGER.info("🔒 [ws_auth] Connexion WebSocket authentifiée pour: {}", username);
      } else {
        throw new org.springframework.messaging.MessageDeliveryException("Invalid JWT token");
      }
    } catch (org.springframework.messaging.MessageDeliveryException e) {
      throw e;
    } catch (Exception e) {
      LOGGER.error("🔒 [ws_auth] Erreur d'authentification WebSocket: {}", e.getMessage());
      throw new org.springframework.messaging.MessageDeliveryException("Authentication failed: " + e.getMessage());
    }
  }

  private void handleSubscribe(StompHeaderAccessor accessor) {
    String destination = accessor.getDestination();
    if (destination == null) {
      return;
    }

    // Récupérer l'utilisateur authentifié
    java.security.Principal principal = accessor.getUser();
    if (principal == null) {
      LOGGER.warn("🔒 [ws_subscribe] Abonnement refusé : utilisateur non authentifié, destination={}", destination);
      throw new org.springframework.messaging.MessageDeliveryException("Authentication required for subscription");
    }

    // Vérifier l'accès aux conversations privées
    Matcher conversationMatcher = CONVERSATION_QUEUE_PATTERN.matcher(destination);
    if (conversationMatcher.matches()) {
      Long conversationId = Long.parseLong(conversationMatcher.group(1));
      validateConversationAccess(principal, conversationId);
      return;
    }

    // Vérifier l'accès aux topics de chat saloon
    Matcher saloonChatMatcher = SALOON_CHAT_PATTERN.matcher(destination);
    if (saloonChatMatcher.matches()) {
      Long saloonId = Long.parseLong(saloonChatMatcher.group(1));
      validateSaloonAccess(principal, saloonId);
      return;
    }

    // Vérifier l'accès aux topics de présence saloon
    Matcher saloonPresenceMatcher = SALOON_PRESENCE_PATTERN.matcher(destination);
    if (saloonPresenceMatcher.matches()) {
      Long saloonId = Long.parseLong(saloonPresenceMatcher.group(1));
      validateSaloonAccess(principal, saloonId);
      return;
    }

    // Topics publics : autorisés (ex: /topic/public)
  }

  private void validateConversationAccess(java.security.Principal principal, Long conversationId) {
    String email = principal.getName();
    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) {
      throw new org.springframework.messaging.MessageDeliveryException("User not found");
    }

    Optional<Conversation> convOpt = conversationRepository.findByIdWithParticipants(conversationId);
    if (convOpt.isEmpty()) {
      throw new org.springframework.messaging.MessageDeliveryException("Conversation not found");
    }

    Conversation conversation = convOpt.get();
    if (conversation.getParticipant(userOpt.get().getId()) == null) {
      LOGGER.warn("🔒 [ws_subscribe] Accès refusé à conversation={} pour user={}", conversationId, email);
      throw new org.springframework.messaging.MessageDeliveryException("Access denied to this conversation");
    }
  }

  private void validateSaloonAccess(java.security.Principal principal, Long saloonId) {
    String email = principal.getName();
    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) {
      throw new org.springframework.messaging.MessageDeliveryException("User not found");
    }

    Optional<Long> userSaloonId = sessionRedisService.getSessionSaloonId(userOpt.get().getId());
    if (userSaloonId.isEmpty() || !userSaloonId.get().equals(saloonId)) {
      LOGGER.warn("🔒 [ws_subscribe] Accès refusé au saloon={} pour user={}", saloonId, email);
      throw new org.springframework.messaging.MessageDeliveryException("Access denied to this saloon");
    }
  }
}

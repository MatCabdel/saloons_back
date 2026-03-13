package com.backend_project_template.domains.session;

import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.conversation.ConversationParticipant;
import com.backend_project_template.domains.conversation.ConversationParticipantRepository;
import com.backend_project_template.domains.conversation.ConversationRepository;
import com.backend_project_template.domains.pushtoken.FcmNotificationService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service centralisé pour l'expiration des conversations lorsqu'un utilisateur
 * quitte un saloon (manuellement ou par timeout).
 *
 * <p>
 * Gère le {@code leftAt} du participant et l'envoi de la notification
 * "conversation expirée" aux deux participants.
 *
 * <p>
 * Idempotence : la notification n'est envoyée que si {@code leftAt} était
 * {@code null} avant la mise à jour (première expiration uniquement).
 */
@Service
public class ConversationExpirationService {

    private static final Logger LOG = LoggerFactory.getLogger(ConversationExpirationService.class);

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final FcmNotificationService fcmNotificationService;

    public ConversationExpirationService(
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            FcmNotificationService fcmNotificationService) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.fcmNotificationService = fcmNotificationService;
    }

    /**
     * Expire toutes les conversations actives d'un utilisateur dans un saloon.
     * Met à jour le {@code leftAt} et envoie la notification push aux deux
     * participants.
     */
    @Transactional
    public void expireConversationsInSaloon(Long userId, Long saloonId) {
        List<Conversation> conversations = conversationRepository
                .findActiveConversationsForUserInSaloon(userId, saloonId);

        LocalDateTime now = LocalDateTime.now();
        for (Conversation conversation : conversations) {
            ConversationParticipant participant = conversation.getParticipant(userId);
            if (participant != null && participant.getLeftAt() == null) {
                participant.setLeftAt(now);
                participantRepository.save(participant);

                // Notifier les deux participants que la conversation a expiré
                sendConversationExpiredNotification(conversation, userId);
            }
        }
    }

    /**
     * Envoie la notification "conversation expirée" à l'autre participant (pas
     * celui qui part).
     * Le message invite à confirmer le coup de cœur pour continuer.
     */
    private void sendConversationExpiredNotification(Conversation conversation, Long leavingUserId) {
        // N'envoyer qu'à l'AUTRE participant (celui qui ne part pas)
        List<Long> otherParticipantIds = conversation.getConversationParticipants().stream()
                .map(cp -> cp.getUser().getId())
                .filter(id -> !id.equals(leavingUserId))
                .collect(Collectors.toList());

        if (otherParticipantIds.isEmpty()) {
            return;
        }

        LOG.info("📩 [conversation_expired_push] conversationId={}, leavingUserId={}, notifyingUsers={}",
                conversation.getId(), leavingUserId, otherParticipantIds);

        try {
            String title = "Saloons";
            String body = "Une conversation a expiré. Confirmez le coup de cœur pour continuer\u00A0!";

            Map<String, String> data = new HashMap<>();
            data.put("type", "conversation_expired");
            data.put("conversationId", String.valueOf(conversation.getId()));

            fcmNotificationService.sendToUsers(otherParticipantIds, title, body, data);
            LOG.info("📩 [conversation_expired_push_sent] conversationId={}", conversation.getId());
        } catch (Exception e) {
            LOG.error("📩 [conversation_expired_push_error] conversationId={}, error={}",
                    conversation.getId(), e.getMessage(), e);
        }
    }
}

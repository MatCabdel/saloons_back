package com.backend_project_template.domains.session;

import com.backend_project_template.domains.presence.dto.PresenceEventDTO;
import com.backend_project_template.domains.presence.dto.SessionAlertDTO;
import com.backend_project_template.domains.presence.dto.UserPresenceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Handler WebSocket pour broadcaster les événements de présence en temps réel.
 */
@Service
public class PresenceWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PresenceWebSocketHandler.class);
    private static final String PRESENCE_TOPIC = "/topic/saloon/%d/presence";
    private static final String SESSION_QUEUE = "/queue/session";

    private final SimpMessagingTemplate messagingTemplate;

    public PresenceWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast un événement USER_JOINED à tous les utilisateurs du saloon.
     */
    public void broadcastUserJoined(Long saloonId, UserPresenceDTO user, int connectedCount) {
        PresenceEventDTO event = new PresenceEventDTO("USER_JOINED", saloonId, user, connectedCount);
        String destination = String.format(PRESENCE_TOPIC, saloonId);

        log.info("Broadcasting USER_JOINED for user {} to saloon {}", user.getId(), saloonId);
        messagingTemplate.convertAndSend(destination, event);
    }

    /**
     * Broadcast un événement USER_LEFT à tous les utilisateurs du saloon.
     */
    public void broadcastUserLeft(Long saloonId, Long userId, int connectedCount) {
        PresenceEventDTO event = new PresenceEventDTO("USER_LEFT", saloonId, userId, connectedCount);
        String destination = String.format(PRESENCE_TOPIC, saloonId);

        log.info("Broadcasting USER_LEFT for user {} from saloon {}", userId, saloonId);
        messagingTemplate.convertAndSend(destination, event);
    }

    /**
     * Notifie un utilisateur spécifique que sa session expire bientôt.
     */
    public void notifySessionExpiringSoon(Long userId, Long saloonId, int remainingMinutes) {
        SessionAlertDTO alert = new SessionAlertDTO("SESSION_EXPIRING_SOON", saloonId, remainingMinutes);

        log.info("Notifying user {} that session in saloon {} expires in {} minutes",
                userId, saloonId, remainingMinutes);
        messagingTemplate.convertAndSendToUser(userId.toString(), SESSION_QUEUE, alert);
    }

    /**
     * Notifie un utilisateur spécifique que sa session a expiré.
     */
    public void notifySessionExpired(Long userId, Long saloonId) {
        SessionAlertDTO alert = new SessionAlertDTO("SESSION_EXPIRED", saloonId, 0);

        log.info("Notifying user {} that session in saloon {} has expired", userId, saloonId);
        messagingTemplate.convertAndSendToUser(userId.toString(), SESSION_QUEUE, alert);
    }
}

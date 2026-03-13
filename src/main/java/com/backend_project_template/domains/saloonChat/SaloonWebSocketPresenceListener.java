package com.backend_project_template.domains.saloonChat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SaloonWebSocketPresenceListener {
    @Autowired
    private SaloonPresenceService presenceService;

    // Map sessionId -> saloonId pour tracker les sessions
    private final Map<String, Long> sessionSaloonMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String destination = sha.getDestination();
        String sessionId = sha.getSessionId();

        // Détecter l'abonnement au topic de présence du saloon
        if (destination != null && destination.startsWith("/topic/saloon-presence/") && sessionId != null) {
            String saloonIdStr = destination.replace("/topic/saloon-presence/", "");
            try {
                Long saloonId = Long.parseLong(saloonIdStr);
                // Éviter les doublons si déjà enregistré pour ce saloon
                if (!sessionSaloonMap.containsKey(sessionId)) {
                    sessionSaloonMap.put(sessionId, saloonId);
                    presenceService.userJoined(saloonId);
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();

        if (sessionId != null && sessionSaloonMap.containsKey(sessionId)) {
            Long saloonId = sessionSaloonMap.remove(sessionId);
            presenceService.userLeft(saloonId);
        }
    }
}

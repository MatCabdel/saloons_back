package com.backend_project_template.domains.saloonChat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SaloonPresenceService {
    private static final int CHAT_ACTIVATION_THRESHOLD = 3;
    private static final int CHAT_DEACTIVATION_DELAY_MINUTES = 5;
    private static final int PRESENCE_CHECK_INTERVAL_MS = 60000;

    private final Map<Long, Integer> saloonPresence = new ConcurrentHashMap<>();
    // Track si le chat a été activé au moins une fois (≥3 participants)
    private final Map<Long, Boolean> chatWasActivated = new ConcurrentHashMap<>();
    // Track le moment où le saloon est passé à 1 participant (pour le délai de 5
    // min)
    private final Map<Long, Instant> lowPresenceSince = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public synchronized void userJoined(Long saloonId) {
        int count = saloonPresence.getOrDefault(saloonId, 0) + 1;
        saloonPresence.put(saloonId, count);

        // Si on atteint le seuil, marquer le chat comme activé
        if (count >= CHAT_ACTIVATION_THRESHOLD) {
            chatWasActivated.put(saloonId, true);
            lowPresenceSince.remove(saloonId); // Réinitialiser le timer
        } else if (count >= 2) {
            // Plus d'1 participant, réinitialiser le timer de désactivation
            lowPresenceSince.remove(saloonId);
        }

        // Note: Ne PAS mettre à jour visitorNumber ici, c'est géré par
        // Redis/SessionRedisService
        broadcastPresence(saloonId);
    }

    public synchronized void userLeft(Long saloonId) {
        int count = Math.max(0, saloonPresence.getOrDefault(saloonId, 1) - 1);
        saloonPresence.put(saloonId, count);

        // Si on tombe à 0, désactiver immédiatement
        if (count == 0) {
            chatWasActivated.put(saloonId, false);
            lowPresenceSince.remove(saloonId);
        }
        // Si on tombe à 1, démarrer le timer de désactivation
        else if (count == 1 && chatWasActivated.getOrDefault(saloonId, false)) {
            lowPresenceSince.putIfAbsent(saloonId, Instant.now());
        }

        // Note: Ne PAS mettre à jour visitorNumber ici, c'est géré par
        // Redis/SessionRedisService
        broadcastPresence(saloonId);
    }

    public int getPresence(Long saloonId) {
        return saloonPresence.getOrDefault(saloonId, 0);
    }

    public boolean isChatEnabled(Long saloonId) {
        int count = getPresence(saloonId);
        boolean wasActivated = chatWasActivated.getOrDefault(saloonId, false);

        // Chat actif si ≥3 participants OU si déjà activé et pas encore désactivé
        if (count >= CHAT_ACTIVATION_THRESHOLD) {
            return true;
        }
        if (wasActivated && count >= 2) {
            return true; // Maintien si ≥2 et déjà activé
        }
        if (wasActivated && count == 1) {
            // Vérifier si le délai de 5 min est écoulé
            Instant since = lowPresenceSince.get(saloonId);
            if (since != null) {
                long minutesElapsed = java.time.Duration.between(since, Instant.now()).toMinutes();
                return minutesElapsed < CHAT_DEACTIVATION_DELAY_MINUTES;
            }
            return true; // Timer pas encore démarré, garder actif
        }
        return false;
    }

    // Job pour vérifier périodiquement les saloons avec 1 participant depuis 5 min
    @Scheduled(fixedRate = PRESENCE_CHECK_INTERVAL_MS) // Toutes les minutes
    public synchronized void checkLowPresenceSaloons() {
        Instant now = Instant.now();
        for (Map.Entry<Long, Instant> entry : lowPresenceSince.entrySet()) {
            Long saloonId = entry.getKey();
            Instant since = entry.getValue();
            long minutesElapsed = java.time.Duration.between(since, now).toMinutes();

            if (minutesElapsed >= CHAT_DEACTIVATION_DELAY_MINUTES) {
                // Désactiver le chat
                chatWasActivated.put(saloonId, false);
                lowPresenceSince.remove(saloonId);
                broadcastPresence(saloonId); // Notifier les clients
            }
        }
    }

    private void broadcastPresence(Long saloonId) {
        int count = getPresence(saloonId);
        boolean enabled = isChatEnabled(saloonId);

        // Broadcast au saloon spécifique (pour le chat uniquement)
        // Note: Ne PAS broadcaster sur /topic/saloon-presence-all ici
        // car ce compteur est pour le chat, pas pour la présence globale (Redis)
        messagingTemplate.convertAndSend("/topic/saloon-presence/" + saloonId,
                new SaloonPresenceDTO(saloonId, count, enabled));
    }
}

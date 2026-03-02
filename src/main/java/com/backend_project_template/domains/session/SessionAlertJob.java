package com.backend_project_template.domains.session;

import com.backend_project_template.domains.presence.dto.ActiveSessionDTO;
import com.backend_project_template.domains.pushtoken.FcmNotificationService;
import com.backend_project_template.infrastructure.redis.RedisKeyBuilder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Job planifié qui envoie une notification push "15 minutes restantes"
 * aux utilisateurs dont la session expire bientôt.
 *
 * <p>Idempotence : utilise une clé Redis {@code session:alert15:user:{userId}}
 * avec {@code setIfAbsent} pour ne jamais notifier deux fois la même session.
 */
@Service
@SuppressWarnings("checkstyle:MagicNumber")
public class SessionAlertJob {

    private static final Logger LOG = LoggerFactory.getLogger(SessionAlertJob.class);

    /** Intervalle d'exécution du job : 30 secondes. */
    private static final int CHECK_INTERVAL_MS = 30_000;

    /** Minutes restantes à partir desquelles on envoie l'alerte. */
    private static final long ALERT_THRESHOLD_MINUTES = RedisKeyBuilder.SESSION_WARNING_MINUTES;

    private final SessionRedisService redisService;
    private final FcmNotificationService fcmNotificationService;

    public SessionAlertJob(SessionRedisService redisService,
            FcmNotificationService fcmNotificationService) {
        this.redisService = redisService;
        this.fcmNotificationService = fcmNotificationService;
    }

    /**
     * Parcourt toutes les sessions actives et envoie une alerte push
     * lorsqu'il reste 15 minutes ou moins.
     */
    @Scheduled(fixedRate = CHECK_INTERVAL_MS)
    public void checkSessionsForAlert() {
        Set<String> sessionKeys = redisService.getAllSessionKeys();

        for (String sessionKey : sessionKeys) {
            Long userId = extractUserIdFromKey(sessionKey);
            if (userId == null) {
                continue;
            }

            // Vérifier si l'alerte a déjà été envoyée (lecture rapide)
            if (redisService.hasSessionAlertBeenSent(userId)) {
                continue;
            }

            Optional<ActiveSessionDTO> sessionOpt = redisService.getActiveSession(userId);
            if (sessionOpt.isEmpty()) {
                continue;
            }

            ActiveSessionDTO session = sessionOpt.get();
            long minutesRemaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), session.getEndsAt());

            if (minutesRemaining <= ALERT_THRESHOLD_MINUTES && minutesRemaining > 0) {
                // Atomique : setIfAbsent garantit qu'un seul thread/instance envoie
                boolean firstToMark = redisService.markSessionAlertSent(userId);
                if (firstToMark) {
                    sendSessionAlert(userId, session.getSaloonName(), minutesRemaining);
                }
            }
        }
    }

    private void sendSessionAlert(Long userId, String saloonName, long minutesRemaining) {
        LOG.info("⏰ [session_alert_15min] userId={}, saloon='{}', minutesLeft={}",
                userId, saloonName, minutesRemaining);
        try {
            String title = "Saloons";
            String body = "Il vous reste 15 minutes pour profiter du Saloon\u00A0!";

            Map<String, String> data = new HashMap<>();
            data.put("type", "saloon_15min");
            data.put("saloonName", saloonName != null ? saloonName : "");

            fcmNotificationService.sendToUser(userId, title, body, data);
            LOG.info("⏰ [session_alert_15min_sent] userId={}", userId);
        } catch (Exception e) {
            LOG.error("⏰ [session_alert_15min_error] userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    private Long extractUserIdFromKey(String key) {
        try {
            return RedisKeyBuilder.extractUserIdFromSessionKey(key);
        } catch (NumberFormatException e) {
            LOG.warn("⏰ Invalid session key format: {}", key);
            return null;
        }
    }
}

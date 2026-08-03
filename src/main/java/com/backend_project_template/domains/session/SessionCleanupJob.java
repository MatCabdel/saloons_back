package com.backend_project_template.domains.session;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Job de nettoyage périodique pour synchroniser la présence Redis.
 * Retire les utilisateurs de la présence des saloons si leur session a expiré.
 */
@Service
@SuppressWarnings("checkstyle:MagicNumber")
public class SessionCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(SessionCleanupJob.class);
    private static final int CLEANUP_INTERVAL_MS = 60000; // 1 minute

    private final SessionRedisService redisService;
    private final ConversationExpirationService conversationExpirationService;
    private final SaloonSessionService saloonSessionService;

    public SessionCleanupJob(
            SessionRedisService redisService,
            ConversationExpirationService conversationExpirationService,
            SaloonSessionService saloonSessionService) {
        this.redisService = redisService;
        this.conversationExpirationService = conversationExpirationService;
        this.saloonSessionService = saloonSessionService;
    }

    /**
     * Vérifie et nettoie les utilisateurs dont la session a expiré
     * mais qui sont encore dans la liste de présence d'un saloon.
     */
    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
    @Transactional
    public void cleanupExpiredPresences() {
        Set<String> presenceKeys = redisService.getAllPresenceKeys();

        for (String presenceKey : presenceKeys) {
            // Extraire le saloonId de la clé (format: "presence:saloon:{saloonId}")
            Long saloonId = extractSaloonIdFromKey(presenceKey);
            if (saloonId == null) {
                continue;
            }

            // Récupérer tous les userIds dans ce saloon
            Set<String> userIds = redisService.getPresenceUserIds(saloonId);

            for (String userIdStr : userIds) {
                Long userId = parseUserId(userIdStr);
                if (userId == null) {
                    continue;
                }

                // Vérifier si l'utilisateur a encore une session active
                if (!redisService.hasActiveSession(userId)) {
                    log.info("🧹 Cleaning up expired presence: userId={} from saloonId={}", userId, saloonId);
                    conversationExpirationService.expireConversationsInSaloon(userId, saloonId);
                    saloonSessionService.closeHistoricalSession(userId, saloonId, java.time.LocalDateTime.now());
                    redisService.removeFromPresence(saloonId, userId);
                }
            }
        }
    }

    private Long parseUserId(String userIdStr) {
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid userId format in presence set: {}", userIdStr);
            return null;
        }
    }

    private Long extractSaloonIdFromKey(String key) {
        try {
            // Format: "presence:saloon:{saloonId}"
            String[] parts = key.split(":");
            if (parts.length >= 3) {
                return Long.parseLong(parts[2]);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid presence key format: {}", key);
        }
        return null;
    }
}

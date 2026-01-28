package com.backend_project_template.domains.session;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    public SessionCleanupJob(SessionRedisService redisService) {
        this.redisService = redisService;
    }

    /**
     * Vérifie et nettoie les utilisateurs dont la session a expiré
     * mais qui sont encore dans la liste de présence d'un saloon.
     */
    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
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
                Long userId = Long.parseLong(userIdStr);

                // Vérifier si l'utilisateur a encore une session active
                if (!redisService.hasActiveSession(userId)) {
                    log.info("🧹 Cleaning up expired presence: userId={} from saloonId={}", userId, saloonId);
                    redisService.removeFromPresence(saloonId, userId);
                }
            }
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

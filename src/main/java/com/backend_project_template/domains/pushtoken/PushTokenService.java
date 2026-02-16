package com.backend_project_template.domains.pushtoken;

import com.backend_project_template.domains.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PushTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushTokenService.class);

    private final PushTokenRepository pushTokenRepository;

    public PushTokenService(PushTokenRepository pushTokenRepository) {
        this.pushTokenRepository = pushTokenRepository;
    }

    /**
     * Enregistre ou met à jour un token FCM pour un utilisateur.
     * Si le token existe déjà pour un autre user, il est réassigné.
     * Si le token existe pour le même user, on le réactive si inactif.
     */
    @Transactional
    public PushToken registerToken(String token, String platform, User user) {
        LOGGER.info("📱 Registering push token for user {} on platform {}", user.getId(), platform);

        Optional<PushToken> existingToken = pushTokenRepository.findByToken(token);

        if (existingToken.isPresent()) {
            PushToken pushToken = existingToken.get();

            // Si le token appartient à un autre utilisateur, le réassigner
            if (!pushToken.getUser().getId().equals(user.getId())) {
                LOGGER.info("📱 Token already exists for another user, reassigning to user {}", user.getId());
                pushToken.setUser(user);
            }

            // Réactiver si inactif
            if (!pushToken.getActive()) {
                LOGGER.info("📱 Reactivating inactive token");
                pushToken.setActive(true);
            }

            pushToken.setPlatform(platform);
            return pushTokenRepository.save(pushToken);
        }

        // Créer un nouveau token
        PushToken newToken = new PushToken(token, platform, user);
        return pushTokenRepository.save(newToken);
    }

    /**
     * Désactive un token (appelé lors du logout ou si FCM signale une erreur)
     */
    @Transactional
    public void deactivateToken(String token) {
        LOGGER.info("📱 Deactivating push token");
        int updated = pushTokenRepository.deactivateToken(token);
        if (updated > 0) {
            LOGGER.info("📱 Token deactivated successfully");
        } else {
            LOGGER.warn("📱 Token not found for deactivation");
        }
    }

    /**
     * Récupère tous les tokens actifs d'un utilisateur
     */
    public List<PushToken> getActiveTokensForUser(Long userId) {
        return pushTokenRepository.findByUserIdAndActiveTrue(userId);
    }

    /**
     * Récupère tous les tokens actifs pour une liste d'utilisateurs
     */
    public List<PushToken> getActiveTokensForUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return pushTokenRepository.findActiveTokensByUserIds(userIds);
    }

    /**
     * Désactive les tokens invalides (appelé après erreur FCM)
     */
    private static final int TOKEN_LOG_LENGTH = 20;

    @Transactional
    public void deactivateInvalidTokens(List<String> invalidTokens) {
        for (String token : invalidTokens) {
            LOGGER.warn("📱 Deactivating invalid token: {}...", token.substring(0, Math.min(TOKEN_LOG_LENGTH, token.length())));
            pushTokenRepository.deactivateToken(token);
        }
    }
}

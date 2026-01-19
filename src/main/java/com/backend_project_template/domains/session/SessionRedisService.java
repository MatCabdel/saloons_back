package com.backend_project_template.domains.session;

import com.backend_project_template.domains.presence.dto.ActiveSessionDTO;
import com.backend_project_template.domains.saloonChat.SaloonPresenceDTO;
import com.backend_project_template.infrastructure.redis.RedisKeyBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service d'accès Redis pour la gestion des sessions et de la présence.
 */
@Service
@SuppressWarnings("checkstyle:ParameterNumber")
public class SessionRedisService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final int KEY_PARTS_MIN_LENGTH = 3;
    private static final int SALOON_ID_PART_INDEX = 2;

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public SessionRedisService(StringRedisTemplate stringRedisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    // ==================== SESSION ====================

    /**
     * Crée une nouvelle session pour un utilisateur.
     */
    public void createSession(Long userId, Long saloonId, String saloonName,
            LocalDateTime joinedAt, LocalDateTime endsAt) {
        String key = RedisKeyBuilder.sessionKey(userId);
        Map<String, String> sessionData = new HashMap<>();
        sessionData.put("saloonId", saloonId.toString());
        sessionData.put("saloonName", saloonName);
        sessionData.put("joinedAt", joinedAt.format(FORMATTER));
        sessionData.put("endsAt", endsAt.format(FORMATTER));

        stringRedisTemplate.opsForHash().putAll(key, sessionData);
        stringRedisTemplate.expire(key, Duration.ofSeconds(RedisKeyBuilder.SESSION_TTL_SECONDS));
    }

    /**
     * Récupère la session active d'un utilisateur.
     */
    public Optional<ActiveSessionDTO> getActiveSession(Long userId) {
        String key = RedisKeyBuilder.sessionKey(userId);
        Map<Object, Object> data = stringRedisTemplate.opsForHash().entries(key);

        if (data.isEmpty()) {
            return Optional.empty();
        }

        LocalDateTime joinedAt = LocalDateTime.parse((String) data.get("joinedAt"), FORMATTER);
        LocalDateTime endsAt = LocalDateTime.parse((String) data.get("endsAt"), FORMATTER);

        // Vérifier si la session est encore active
        if (LocalDateTime.now().isAfter(endsAt)) {
            deleteSession(userId);
            return Optional.empty();
        }

        return Optional.of(new ActiveSessionDTO(
                userId,
                Long.parseLong((String) data.get("saloonId")),
                (String) data.get("saloonName"),
                joinedAt,
                endsAt));
    }

    /**
     * Supprime la session d'un utilisateur.
     */
    public void deleteSession(Long userId) {
        stringRedisTemplate.delete(RedisKeyBuilder.sessionKey(userId));
    }

    /**
     * Vérifie si un utilisateur a une session active.
     */
    public boolean hasActiveSession(Long userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisKeyBuilder.sessionKey(userId)));
    }

    /**
     * Récupère le saloonId de la session active d'un utilisateur.
     */
    public Optional<Long> getSessionSaloonId(Long userId) {
        String key = RedisKeyBuilder.sessionKey(userId);
        Object saloonId = stringRedisTemplate.opsForHash().get(key, "saloonId");
        return saloonId != null ? Optional.of(Long.parseLong((String) saloonId)) : Optional.empty();
    }

    // ==================== PRESENCE ====================

    /**
     * Ajoute un utilisateur à la présence d'un saloon.
     */
    public void addToPresence(Long saloonId, Long userId) {
        System.out.println("➕ addToPresence called: saloonId=" + saloonId + ", userId=" + userId);
        stringRedisTemplate.opsForSet().add(
                RedisKeyBuilder.presenceKey(saloonId),
                userId.toString());
        broadcastPresenceUpdate(saloonId);
    }

    /**
     * Retire un utilisateur de la présence d'un saloon.
     */
    public void removeFromPresence(Long saloonId, Long userId) {
        System.out.println("➖ removeFromPresence called: saloonId=" + saloonId + ", userId=" + userId);
        stringRedisTemplate.opsForSet().remove(
                RedisKeyBuilder.presenceKey(saloonId),
                userId.toString());
        broadcastPresenceUpdate(saloonId);
    }

    /**
     * Broadcast la mise à jour de présence via WebSocket
     */
    private void broadcastPresenceUpdate(Long saloonId) {
        int count = getPresenceCount(saloonId);
        System.out.println("📡 Broadcasting presence update: saloonId=" + saloonId + ", count=" + count);
        // Broadcast global pour la liste des saloons
        messagingTemplate.convertAndSend("/topic/saloon-presence-all",
                new SaloonPresenceDTO(saloonId, count, true));
    }

    /**
     * Récupère tous les userIds connectés à un saloon.
     */
    public Set<String> getPresenceUserIds(Long saloonId) {
        Set<String> members = stringRedisTemplate.opsForSet().members(RedisKeyBuilder.presenceKey(saloonId));
        return members != null ? members : Set.of();
    }

    /**
     * Compte le nombre d'utilisateurs connectés à un saloon.
     */
    public int getPresenceCount(Long saloonId) {
        Long count = stringRedisTemplate.opsForSet().size(RedisKeyBuilder.presenceKey(saloonId));
        return count != null ? count.intValue() : 0;
    }

    // ==================== COOLDOWN ====================

    /**
     * Définit un cooldown pour un utilisateur sur un saloon.
     */
    public void setCooldown(Long userId, Long saloonId) {
        String key = RedisKeyBuilder.cooldownKey(userId, saloonId);
        stringRedisTemplate.opsForValue().set(key, "1",
                Duration.ofSeconds(RedisKeyBuilder.COOLDOWN_TTL_SECONDS));
    }

    /**
     * Vérifie si un utilisateur est en cooldown sur un saloon.
     */
    public boolean hasCooldown(Long userId, Long saloonId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(RedisKeyBuilder.cooldownKey(userId, saloonId)));
    }

    /**
     * Récupère le TTL restant du cooldown en secondes.
     */
    public long getCooldownRemainingSeconds(Long userId, Long saloonId) {
        Long ttl = stringRedisTemplate.getExpire(RedisKeyBuilder.cooldownKey(userId, saloonId));
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    // ==================== COOLDOWN GLOBAL ====================

    /**
     * Définit un cooldown global pour un utilisateur (tous saloons).
     * Le cooldown expire à minuit le lendemain.
     */
    public void setGlobalCooldown(Long userId) {
        String key = RedisKeyBuilder.globalCooldownKey(userId);
        // Calculer le temps jusqu'à minuit
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        long secondsUntilMidnight = java.time.Duration.between(now, midnight).getSeconds();

        stringRedisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(secondsUntilMidnight));
    }

    /**
     * Vérifie si un utilisateur a un cooldown global actif.
     */
    public boolean hasGlobalCooldown(Long userId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(RedisKeyBuilder.globalCooldownKey(userId)));
    }

    /**
     * Récupère le TTL restant du cooldown global en secondes.
     */
    public long getGlobalCooldownRemainingSeconds(Long userId) {
        Long ttl = stringRedisTemplate.getExpire(RedisKeyBuilder.globalCooldownKey(userId));
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    // ==================== USER CACHE ====================

    /**
     * Met en cache les informations d'un utilisateur.
     */
    public void cacheUserInfo(Long userId, String userName, String imgUrl, Integer age, String city) {
        String key = RedisKeyBuilder.userCacheKey(userId);
        Map<String, String> userData = new HashMap<>();
        userData.put("userName", userName);
        userData.put("imgUrl", imgUrl != null ? imgUrl : "");
        userData.put("age", age != null ? String.valueOf(age) : "");
        userData.put("city", city != null ? city : "");

        stringRedisTemplate.opsForHash().putAll(key, userData);
        stringRedisTemplate.expire(key, Duration.ofSeconds(RedisKeyBuilder.USER_CACHE_TTL_SECONDS));
    }

    /**
     * Récupère les informations cachées d'un utilisateur.
     */
    public Optional<Map<String, String>> getCachedUserInfo(Long userId) {
        String key = RedisKeyBuilder.userCacheKey(userId);
        Map<Object, Object> data = stringRedisTemplate.opsForHash().entries(key);

        if (data.isEmpty()) {
            return Optional.empty();
        }

        Map<String, String> result = new HashMap<>();
        data.forEach((k, v) -> result.put((String) k, (String) v));
        return Optional.of(result);
    }

    // ==================== ADMIN / STATS ====================

    /**
     * Compte le nombre total d'utilisateurs connectés (toutes sessions).
     */
    public int getTotalConnectedUsers() {
        Set<String> keys = stringRedisTemplate.keys(RedisKeyBuilder.sessionPattern());
        return keys != null ? keys.size() : 0;
    }

    /**
     * Récupère toutes les clés de présence (pour stats par saloon).
     */
    public Set<String> getAllPresenceKeys() {
        Set<String> keys = stringRedisTemplate.keys(RedisKeyBuilder.presencePattern());
        return keys != null ? keys : Set.of();
    }

    /**
     * Retire un utilisateur de la présence de TOUS les saloons.
     * Utilisé lors de la suppression d'un utilisateur.
     */
    public void removeUserFromAllPresence(Long userId) {
        Set<String> keys = getAllPresenceKeys();
        String userIdStr = userId.toString();

        for (String key : keys) {
            // Vérifier si l'utilisateur est dans ce saloon
            Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userIdStr);
            if (Boolean.TRUE.equals(isMember)) {
                // Extraire le saloonId de la clé (format: "presence:saloon:{saloonId}")
                try {
                    String[] parts = key.split(":");
                    if (parts.length >= KEY_PARTS_MIN_LENGTH) {
                        Long saloonId = Long.parseLong(parts[SALOON_ID_PART_INDEX]);
                        removeFromPresence(saloonId, userId);
                        System.out.println("🧹 Removed user " + userId + " from presence of saloon " + saloonId);
                    }
                } catch (NumberFormatException e) {
                    // Ignorer les clés mal formatées
                }
            }
        }
    }

    /**
     * Récupère tous les compteurs de présence pour tous les saloons.
     * 
     * @return Map avec saloonId comme clé et le nombre de connectés comme valeur
     */
    public Map<Long, Integer> getAllPresenceCounts() {
        Map<Long, Integer> counts = new HashMap<>();
        Set<String> keys = getAllPresenceKeys();

        for (String key : keys) {
            // Extraire le saloonId de la clé (format: "saloon:presence:{saloonId}")
            try {
                String[] parts = key.split(":");
                if (parts.length >= KEY_PARTS_MIN_LENGTH) {
                    Long saloonId = Long.parseLong(parts[SALOON_ID_PART_INDEX]);
                    Long count = stringRedisTemplate.opsForSet().size(key);
                    counts.put(saloonId, count != null ? count.intValue() : 0);
                }
            } catch (NumberFormatException e) {
                // Ignorer les clés mal formatées
            }
        }

        return counts;
    }
}

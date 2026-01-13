package com.backend_project_template.infrastructure.redis;

/**
 * Utilitaire pour construire les clés Redis de manière cohérente.
 * Convention de nommage: {domain}:{entity}:{id}[:subkey]
 */
@SuppressWarnings("checkstyle:DeclarationOrder")
public class RedisKeyBuilder {

    // Préfixes
    private static final String SESSION_PREFIX = "session:user:";
    private static final String PRESENCE_PREFIX = "presence:saloon:";
    private static final String COOLDOWN_PREFIX = "cooldown:user:";
    private static final String GLOBAL_COOLDOWN_PREFIX = "cooldown:global:user:";
    private static final String USER_CACHE_PREFIX = "user:info:";

    // TTL en secondes
    /** TTL de session: 3 heures. */
    public static final long SESSION_TTL_SECONDS = 3 * 60 * 60;
    /** TTL de cooldown: 30 secondes pour les tests (TODO: 24h en prod). */
    public static final long COOLDOWN_TTL_SECONDS = 30;
    /** TTL du cache utilisateur: 1 heure. */
    public static final long USER_CACHE_TTL_SECONDS = 60 * 60;
    /** Alerte avant expiration: 15 minutes. */
    public static final int SESSION_WARNING_MINUTES = 15;

    private RedisKeyBuilder() {
        // Utility class
    }

    /**
     * Clé pour la session active d'un utilisateur.
     * Structure: Hash avec saloonId, saloonName, joinedAt, endsAt
     * TTL: 3 heures
     */
    public static String sessionKey(Long userId) {
        return SESSION_PREFIX + userId;
    }

    /**
     * Clé pour la présence dans un saloon.
     * Structure: Set de userIds
     * Pas de TTL (nettoyé manuellement)
     */
    public static String presenceKey(Long saloonId) {
        return PRESENCE_PREFIX + saloonId;
    }

    /**
     * Clé pour le cooldown d'un utilisateur sur un saloon spécifique.
     * Structure: Simple string "1"
     * TTL: 24 heures
     */
    public static String cooldownKey(Long userId, Long saloonId) {
        return COOLDOWN_PREFIX + userId + ":saloon:" + saloonId;
    }

    /**
     * Clé pour le cooldown global d'un utilisateur (tous saloons).
     * Structure: Simple string "1"
     * TTL: jusqu'au lendemain
     */
    public static String globalCooldownKey(Long userId) {
        return GLOBAL_COOLDOWN_PREFIX + userId;
    }

    /**
     * Clé pour le cache des informations utilisateur.
     * Structure: Hash avec userName, imgUrl
     * TTL: 1 heure
     */
    public static String userCacheKey(Long userId) {
        return USER_CACHE_PREFIX + userId;
    }

    /**
     * Pattern pour récupérer toutes les clés de présence.
     */
    public static String presencePattern() {
        return PRESENCE_PREFIX + "*";
    }

    /**
     * Pattern pour récupérer toutes les sessions actives.
     */
    public static String sessionPattern() {
        return SESSION_PREFIX + "*";
    }

    /**
     * Extrait l'ID du saloon depuis une clé de présence.
     */
    public static Long extractSaloonIdFromPresenceKey(String key) {
        return Long.parseLong(key.replace(PRESENCE_PREFIX, ""));
    }

    /**
     * Extrait l'ID de l'utilisateur depuis une clé de session.
     */
    public static Long extractUserIdFromSessionKey(String key) {
        return Long.parseLong(key.replace(SESSION_PREFIX, ""));
    }
}

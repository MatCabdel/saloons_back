package com.backend_project_template.domains.session;

import com.backend_project_template.domains.presence.dto.ActiveSessionDTO;
import com.backend_project_template.domains.presence.dto.JoinResponseDTO;
import com.backend_project_template.domains.presence.dto.UserPresenceDTO;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import com.backend_project_template.domains.user.UserService;
import com.backend_project_template.infrastructure.redis.RedisKeyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service métier pour la gestion des sessions utilisateur dans les saloons.
 * Gère le join, leave, cooldown et les validations.
 */
@Service
@SuppressWarnings({ "checkstyle:ParameterNumber", "checkstyle:MagicNumber" })
public class SaloonSessionService {

    /** Nombre de secondes dans une heure. */
    private static final int SECONDS_PER_HOUR = 3600;
    /** Nombre de secondes dans une minute. */
    private static final int SECONDS_PER_MINUTE = 60;
    /** Rayon de la Terre en mètres. */
    private static final int EARTH_RADIUS_METERS = 6371000;
    /** Diviseur pour formule Haversine. */
    private static final double HAVERSINE_DIVISOR = 2.0;

    private final SessionRedisService redisService;
    private final SaloonRepository saloonRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PresenceWebSocketHandler presenceWebSocketHandler;

    public SaloonSessionService(SessionRedisService redisService,
            SaloonRepository saloonRepository,
            UserRepository userRepository,
            UserService userService,
            PresenceWebSocketHandler presenceWebSocketHandler) {
        this.redisService = redisService;
        this.saloonRepository = saloonRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.presenceWebSocketHandler = presenceWebSocketHandler;
    }

    /**
     * Permet à un utilisateur de rejoindre un saloon.
     * 
     * @param userId   l'ID de l'utilisateur
     * @param saloonId l'ID du saloon
     * @param userLat  la latitude de l'utilisateur
     * @param userLng  la longitude de l'utilisateur
     * @return JoinResponseDTO avec les détails de la session
     * @throws SessionException si les règles métier ne sont pas respectées
     */
    @Transactional
    public JoinResponseDTO joinSaloon(Long userId, Long saloonId, Double userLat, Double userLng) {
        // 1. Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SessionException("Utilisateur non trouvé"));

        // 2. Vérifier que le saloon existe et est actif
        Saloon saloon = saloonRepository.findById(saloonId)
                .orElseThrow(() -> new SessionException("Saloon non trouvé"));

        if (!saloon.getIsActive()) {
            throw new SessionException("Ce saloon n'est pas actif");
        }

        // 3. Vérifier si l'utilisateur a déjà une session active
        if (redisService.hasActiveSession(userId)) {
            Optional<Long> currentSaloonId = redisService.getSessionSaloonId(userId);
            if (currentSaloonId.isPresent() && currentSaloonId.get().equals(saloonId)) {
                // L'utilisateur est déjà dans ce saloon, retourner sa session
                return getCurrentSessionResponse(userId, saloon);
            }
            throw new SessionException("Vous avez déjà une session active dans un autre saloon. Quittez d'abord.");
        }

        // 4. Vérifier le cooldown global (sauf premium) - un seul saloon par jour
        if (!isPremium(user) && redisService.hasGlobalCooldown(userId)) {
            long remainingSeconds = redisService.getGlobalCooldownRemainingSeconds(userId);
            long remainingHours = remainingSeconds / SECONDS_PER_HOUR;
            long remainingMinutes = (remainingSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
            throw new SessionException(
                    "Vous avez déjà visité un saloon aujourd'hui. Revenez demain ! (dans "
                            + remainingHours + "h" + remainingMinutes + "min)");
        }

        // 5. Vérifier la proximité géographique
        // TODO: Réactiver pour la production
        // Temporairement désactivé pour les tests
        /*
         * if (userLat != null && userLng != null) {
         * double distance = calculateDistance(
         * userLat, userLng,
         * saloon.getLatitude().doubleValue(),
         * saloon.getLongitude().doubleValue()
         * );
         * 
         * if (distance > saloon.getRadiusMeters()) {
         * throw new SessionException(
         * "Vous êtes trop loin de ce saloon (" + (int) distance + "m). " +
         * "Rapprochez-vous à moins de " + saloon.getRadiusMeters() + "m."
         * );
         * }
         * }
         */

        // 6. Créer la session
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endsAt = now.plusSeconds(RedisKeyBuilder.SESSION_TTL_SECONDS);

        redisService.createSession(userId, saloonId, saloon.getName(), now, endsAt);
        redisService.addToPresence(saloonId, userId);

        // 7. Mettre en cache les infos utilisateur (avec l'âge et la ville)
        Integer age = userService.calculateAge(user.getBirthDate());
        String city = user.getCity();
        redisService.cacheUserInfo(userId, user.getUserName(), user.getImgUrl(), age, city);

        // 8. Broadcaster l'événement de présence
        UserPresenceDTO userPresence = new UserPresenceDTO(
                userId,
                user.getUserName(),
                user.getImgUrl(),
                age,
                city);
        int connectedCount = redisService.getPresenceCount(saloonId);
        presenceWebSocketHandler.broadcastUserJoined(saloonId, userPresence, connectedCount);

        return new JoinResponseDTO(saloonId, saloon.getName(), now, endsAt, connectedCount);
    }

    /**
     * Permet à un utilisateur de quitter un saloon.
     */
    @Transactional
    public void leaveSaloon(Long userId, Long saloonId) {
        // Vérifier que l'utilisateur a une session dans ce saloon
        Optional<ActiveSessionDTO> session = redisService.getActiveSession(userId);
        if (session.isEmpty() || !session.get().getSaloonId().equals(saloonId)) {
            throw new SessionException("Aucune session active dans ce saloon");
        }

        // Supprimer de la présence
        redisService.removeFromPresence(saloonId, userId);

        // Supprimer la session
        redisService.deleteSession(userId);

        // Définir le cooldown global (jusqu'à minuit)
        redisService.setGlobalCooldown(userId);

        // Broadcaster l'événement de départ
        int connectedCount = redisService.getPresenceCount(saloonId);
        presenceWebSocketHandler.broadcastUserLeft(saloonId, userId, connectedCount);
    }

    /**
     * Force la fin de session (expiration ou déconnexion).
     */
    public void forceLeave(Long userId) {
        Optional<ActiveSessionDTO> session = redisService.getActiveSession(userId);
        if (session.isPresent()) {
            Long saloonId = session.get().getSaloonId();
            redisService.removeFromPresence(saloonId, userId);
            redisService.deleteSession(userId);
            redisService.setGlobalCooldown(userId);

            int connectedCount = redisService.getPresenceCount(saloonId);
            presenceWebSocketHandler.broadcastUserLeft(saloonId, userId, connectedCount);
        }
    }

    /**
     * Récupère la session active d'un utilisateur.
     */
    public Optional<ActiveSessionDTO> getActiveSession(Long userId) {
        return redisService.getActiveSession(userId);
    }

    /**
     * Vérifie si l'utilisateur est premium.
     */
    private boolean isPremium(User user) {
        // TODO: Implémenter la logique premium
        // Pour l'instant, retourne false (tout le monde est freemium)
        return false;
    }

    /**
     * Calcule la distance en mètres entre deux points GPS (formule Haversine).
     */
    @SuppressWarnings("unused")
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / HAVERSINE_DIVISOR) * Math.sin(dLat / HAVERSINE_DIVISOR)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / HAVERSINE_DIVISOR) * Math.sin(dLng / HAVERSINE_DIVISOR);

        double c = HAVERSINE_DIVISOR * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Construit la réponse pour une session existante.
     */
    private JoinResponseDTO getCurrentSessionResponse(Long userId, Saloon saloon) {
        Optional<ActiveSessionDTO> session = redisService.getActiveSession(userId);
        if (session.isEmpty()) {
            throw new SessionException("Session non trouvée");
        }

        ActiveSessionDTO activeSession = session.get();
        int connectedCount = redisService.getPresenceCount(saloon.getId());

        return new JoinResponseDTO(
                saloon.getId(),
                saloon.getName(),
                activeSession.getJoinedAt(),
                activeSession.getEndsAt(),
                connectedCount);
    }
}

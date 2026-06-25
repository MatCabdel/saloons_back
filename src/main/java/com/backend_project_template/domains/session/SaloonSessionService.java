package com.backend_project_template.domains.session;

import com.backend_project_template.domains.presence.dto.ActiveSessionDTO;
import com.backend_project_template.domains.presence.dto.JoinResponseDTO;
import com.backend_project_template.domains.presence.dto.UserPresenceDTO;
import com.backend_project_template.domains.review.ReviewDemoService;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import com.backend_project_template.domains.user.UserService;
import com.backend_project_template.infrastructure.redis.RedisKeyBuilder;
import com.backend_project_template.core.Constant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@SuppressWarnings({ "checkstyle:ParameterNumber", "checkstyle:MagicNumber" })
public class SaloonSessionService {

    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int EARTH_RADIUS_METERS = 6371000;
    private static final double HAVERSINE_DIVISOR = 2.0;

    private final SessionRedisService redisService;
    private final SaloonRepository saloonRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PresenceWebSocketHandler presenceWebSocketHandler;
    private final ConversationExpirationService conversationExpirationService;
    private final ReviewDemoService reviewDemoService;

    public SaloonSessionService(SessionRedisService redisService,
            SaloonRepository saloonRepository,
            UserRepository userRepository,
            UserService userService,
            PresenceWebSocketHandler presenceWebSocketHandler,
            ConversationExpirationService conversationExpirationService,
            ReviewDemoService reviewDemoService) {
        this.redisService = redisService;
        this.saloonRepository = saloonRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.presenceWebSocketHandler = presenceWebSocketHandler;
        this.conversationExpirationService = conversationExpirationService;
        this.reviewDemoService = reviewDemoService;
    }

    @Transactional
    public JoinResponseDTO joinSaloon(Long userId, Long saloonId, Double userLat, Double userLng) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SessionException("Utilisateur non trouvé"));

        Saloon saloon = saloonRepository.findById(saloonId)
                .orElseThrow(() -> new SessionException("Saloon non trouvé"));

        if (!saloon.getIsActive()) {
            throw new SessionException("Ce saloon n'est pas actif");
        }

        boolean canAccessPrivate = canAccessPrivateSaloons(user);
        if (Boolean.TRUE.equals(saloon.getIsPrivate()) && !canAccessPrivate) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce saloon est privé");
        }

        if (redisService.hasActiveSession(userId)) {
            Optional<Long> currentSaloonId = redisService.getSessionSaloonId(userId);
            if (currentSaloonId.isPresent() && currentSaloonId.get().equals(saloonId)) {
                return getCurrentSessionResponse(userId, saloon);
            }
            throw new SessionException("Vous avez déjà une session active dans un autre saloon. Quittez d'abord.");
        }

        // Vérifier si l'utilisateur a une sortie en attente (undo possible)
        boolean hasLeavePending = redisService.hasLeavePending(userId, saloonId);

        // Vérifier le cooldown global pour les freemium (sauf si leave pending actif =
        // undo)
        boolean bypassPremiumRules = canBypassPremiumRules(user, saloon);
        if (!isPremium(user) && !bypassPremiumRules && !hasLeavePending && redisService.hasGlobalCooldown(userId)) {
            long remainingSeconds = redisService.getGlobalCooldownRemainingSeconds(userId);
            long remainingHours = remainingSeconds / SECONDS_PER_HOUR;
            long remainingMinutes = (remainingSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
            throw new SessionException(
                    "Vous avez déjà visité un saloon aujourd'hui. Revenez après 6h du matin ! (dans "
                            + remainingHours + "h" + String.format("%02d", remainingMinutes) + "min)");
        }

        // Si l'utilisateur revient après avoir quitté (undo), supprimer le leave
        // pending
        if (hasLeavePending) {
            redisService.deleteLeavePending(userId, saloonId);
        }

        // Règles de distance : uniquement pour les saloons publics (pas de bypass
        // global)
        Integer radiusMeters = saloon.getRadiusMeters();
        if (!isAdmin(user) && !Boolean.TRUE.equals(saloon.getIsPrivate()) && radiusMeters != null) {
            if (userLat == null || userLng == null) {
                throw new SessionException("Position requise pour entrer dans un saloon public");
            }
            double distance = calculateDistance(
                    userLat, userLng,
                    saloon.getLatitude().doubleValue(),
                    saloon.getLongitude().doubleValue());
            if (distance > radiusMeters) {
                throw new SessionException(
                        "Vous êtes trop loin de ce saloon (" + (int) distance + "m). "
                                + "Rapprochez-vous à moins de " + radiusMeters + "m.");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endsAt = now.plusSeconds(RedisKeyBuilder.SESSION_TTL_SECONDS);

        redisService.createSession(userId, saloonId, saloon.getName(), now, endsAt);
        redisService.addToPresence(saloonId, userId);

        Integer age = userService.calculateAge(user.getBirthDate());
        String city = user.getCity();
        redisService.cacheUserInfo(
                userId,
                user.getUserName(),
                user.getImgUrl(),
                user.getProfileImageUpdatedAt(),
                age,
                city);

        UserPresenceDTO userPresence = new UserPresenceDTO(
                userId,
                user.getUserName(),
                user.getImgUrl(),
                user.getProfileImageUpdatedAt(),
                age,
                city);
        int connectedCount = redisService.getPresenceCount(saloonId);
        if (reviewDemoService.isReviewDemo(user, saloon)) {
            connectedCount = reviewDemoService.ensureReviewConnectedCount(connectedCount);
        }
        presenceWebSocketHandler.broadcastUserJoined(saloonId, userPresence, connectedCount);

        return new JoinResponseDTO(saloonId, saloon.getName(), now, endsAt, connectedCount);
    }

    @Transactional
    public void leaveSaloon(Long userId, Long saloonId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SessionException("Utilisateur non trouvé"));
        Saloon saloon = saloonRepository.findById(saloonId).orElse(null);

        Optional<ActiveSessionDTO> session = redisService.getActiveSession(userId);
        if (session.isEmpty() || !session.get().getSaloonId().equals(saloonId)) {
            throw new SessionException("Aucune session active dans ce saloon");
        }

        // Expirer toutes les conversations actives de l'utilisateur dans ce saloon
        conversationExpirationService.expireConversationsInSaloon(userId, saloonId);

        redisService.removeFromPresence(saloonId, userId);

        redisService.deleteSession(userId);

        if (!canBypassPremiumRules(user, saloon)) {
            redisService.setGlobalCooldown(userId);
        }

        int connectedCount = redisService.getPresenceCount(saloonId);
        presenceWebSocketHandler.broadcastUserLeft(saloonId, userId, connectedCount);
    }

    /**
     * Expire les conversations actives d'un utilisateur dans un saloon.
     * Met à jour le leftAt de la participation de l'utilisateur.
     * 
     * @deprecated Utiliser
     *             {@link ConversationExpirationService#expireConversationsInSaloon}
     *             à la place.
     */
    private void expireConversationsInSaloon(Long userId, Long saloonId) {
        conversationExpirationService.expireConversationsInSaloon(userId, saloonId);
    }

    @Transactional
    public void forceLeave(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SessionException("Utilisateur non trouvé"));
        Optional<ActiveSessionDTO> session = redisService.getActiveSession(userId);
        if (session.isPresent()) {
            Long saloonId = session.get().getSaloonId();
            Saloon saloon = saloonRepository.findById(saloonId).orElse(null);

            // Expirer toutes les conversations actives de l'utilisateur dans ce saloon
            expireConversationsInSaloon(userId, saloonId);

            redisService.removeFromPresence(saloonId, userId);
            redisService.deleteSession(userId);
            if (!canBypassPremiumRules(user, saloon)) {
                redisService.setGlobalCooldown(userId);
            }

            int connectedCount = redisService.getPresenceCount(saloonId);
            presenceWebSocketHandler.broadcastUserLeft(saloonId, userId, connectedCount);
        }
    }

    public Optional<ActiveSessionDTO> getActiveSession(Long userId) {
        return redisService.getActiveSession(userId);
    }

    /**
     * Initie une demande de sortie avec délai d'annulation.
     * Ne supprime PAS immédiatement la session ni n'applique le cooldown.
     * Retourne le timestamp d'expiration du délai d'annulation.
     */
    @Transactional
    public long leaveRequest(Long userId, Long saloonId) {
        Optional<ActiveSessionDTO> session = redisService.getActiveSession(userId);
        if (session.isEmpty() || !session.get().getSaloonId().equals(saloonId)) {
            throw new SessionException("Aucune session active dans ce saloon");
        }

        // Créer la clé leave pending
        long pendingUntil = redisService.setLeavePending(userId, saloonId);

        // Retirer immédiatement de la présence (l'utilisateur n'est plus visible)
        redisService.removeFromPresence(saloonId, userId);
        int connectedCount = redisService.getPresenceCount(saloonId);
        presenceWebSocketHandler.broadcastUserLeft(saloonId, userId, connectedCount);

        // Supprimer la session Redis (l'utilisateur ne peut plus interagir)
        expireConversationsInSaloon(userId, saloonId);
        redisService.deleteSession(userId);

        return pendingUntil;
    }

    /**
     * Annule une sortie en attente.
     * L'utilisateur peut revenir dans le saloon sans consommer de limitation.
     */
    @Transactional
    public void leaveCancel(Long userId, Long saloonId) {
        if (!redisService.hasLeavePending(userId, saloonId)) {
            throw new SessionException("Pas de sortie en attente à annuler");
        }

        // Supprimer la clé leave pending
        redisService.deleteLeavePending(userId, saloonId);
    }

    /**
     * Confirme définitivement la sortie.
     * Applique le cooldown global pour les freemium uniquement.
     */
    @Transactional
    public void leaveConfirm(Long userId, Long saloonId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SessionException("Utilisateur non trouvé"));

        // Supprimer la clé leave pending si elle existe encore
        redisService.deleteLeavePending(userId, saloonId);

        // Appliquer le cooldown global UNIQUEMENT pour les freemium
        Saloon saloon = saloonRepository.findById(saloonId).orElse(null);
        if (!isPremium(user) && !canBypassPremiumRules(user, saloon)) {
            redisService.setGlobalCooldown(userId);
        }
    }

    /**
     * Vérifie si un utilisateur a une sortie en attente.
     */
    public boolean hasLeavePending(Long userId, Long saloonId) {
        return redisService.hasLeavePending(userId, saloonId);
    }

    /**
     * Vérifie si un utilisateur peut rejoindre un saloon.
     * Prend en compte le leave pending pour permettre l'undo.
     */
    public boolean canJoinSaloon(Long userId, Long saloonId, boolean isPremiumUser) {
        User user = userRepository.findById(userId).orElse(null);
        Saloon saloon = saloonRepository.findById(saloonId).orElse(null);

        // Les premium peuvent toujours rejoindre
        if (isPremiumUser || (user != null && canBypassPremiumRules(user, saloon))) {
            return true;
        }

        // Si leave pending actif pour ce saloon, autoriser le retour (undo)
        if (redisService.hasLeavePending(userId, saloonId)) {
            return true;
        }

        // Sinon, vérifier le cooldown global
        return !redisService.hasGlobalCooldown(userId);
    }

    private boolean isPremium(User user) {
        return Boolean.TRUE.equals(user.getIsPremium());
    }

    private boolean isAdmin(User user) {
        return user.getRoles().contains(Constant.ADMIN);
    }

    private boolean isReviewer(User user) {
        return user.getRoles().contains(Constant.REVIEWER);
    }

    private boolean canAccessPrivateSaloons(User user) {
        return isReviewer(user) || isAdmin(user);
    }

    private boolean canBypassPremiumRules(User user, Saloon saloon) {
        if (isAdmin(user)) {
            return true;
        }
        return saloon != null
                && isReviewer(user)
                && Boolean.TRUE.equals(saloon.getIsPrivate());
    }

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

    private JoinResponseDTO getCurrentSessionResponse(Long userId, Saloon saloon) {
        Optional<ActiveSessionDTO> session = redisService.getActiveSession(userId);
        if (session.isEmpty()) {
            throw new SessionException("Session non trouvée");
        }

        ActiveSessionDTO activeSession = session.get();
        int connectedCount = redisService.getPresenceCount(saloon.getId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SessionException("Utilisateur non trouvé"));
        if (reviewDemoService.isReviewDemo(user, saloon)) {
            connectedCount = reviewDemoService.ensureReviewConnectedCount(connectedCount);
        }

        return new JoinResponseDTO(
                saloon.getId(),
                saloon.getName(),
                activeSession.getJoinedAt(),
                activeSession.getEndsAt(),
                connectedCount);
    }
}

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

    @Transactional
    public JoinResponseDTO joinSaloon(Long userId, Long saloonId, Double userLat, Double userLng) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SessionException("Utilisateur non trouvé"));

        Saloon saloon = saloonRepository.findById(saloonId)
                .orElseThrow(() -> new SessionException("Saloon non trouvé"));

        if (!saloon.getIsActive()) {
            throw new SessionException("Ce saloon n'est pas actif");
        }

        if (redisService.hasActiveSession(userId)) {
            Optional<Long> currentSaloonId = redisService.getSessionSaloonId(userId);
            if (currentSaloonId.isPresent() && currentSaloonId.get().equals(saloonId)) {
                return getCurrentSessionResponse(userId, saloon);
            }
            throw new SessionException("Vous avez déjà une session active dans un autre saloon. Quittez d'abord.");
        }

        if (!isPremium(user) && redisService.hasGlobalCooldown(userId)) {
            long remainingSeconds = redisService.getGlobalCooldownRemainingSeconds(userId);
            long remainingHours = remainingSeconds / SECONDS_PER_HOUR;
            long remainingMinutes = (remainingSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
            throw new SessionException(
                    "Vous avez déjà visité un saloon aujourd'hui. Revenez demain ! (dans "
                            + remainingHours + "h" + remainingMinutes + "min)");
        }

        // TODO: Réactiver pour la production
        /*
         * if (userLat != null && userLng != null) {
         * double distance = calculateDistance(
         * userLat, userLng,
         * saloon.getLatitude().doubleValue(),
         * saloon.getLongitude().doubleValue()
         * );
         * if (distance > saloon.getRadiusMeters()) {
         * throw new SessionException(
         * "Vous êtes trop loin de ce saloon (" + (int) distance + "m). " +
         * "Rapprochez-vous à moins de " + saloon.getRadiusMeters() + "m."
         * );
         * }
         * }
         */

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endsAt = now.plusSeconds(RedisKeyBuilder.SESSION_TTL_SECONDS);

        redisService.createSession(userId, saloonId, saloon.getName(), now, endsAt);
        redisService.addToPresence(saloonId, userId);

        Integer age = userService.calculateAge(user.getBirthDate());
        String city = user.getCity();
        redisService.cacheUserInfo(userId, user.getUserName(), user.getImgUrl(), age, city);

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

    @Transactional
    public void leaveSaloon(Long userId, Long saloonId) {

        Optional<ActiveSessionDTO> session = redisService.getActiveSession(userId);
        if (session.isEmpty() || !session.get().getSaloonId().equals(saloonId)) {
            throw new SessionException("Aucune session active dans ce saloon");
        }

        redisService.removeFromPresence(saloonId, userId);

        redisService.deleteSession(userId);

        redisService.setGlobalCooldown(userId);

        int connectedCount = redisService.getPresenceCount(saloonId);
        presenceWebSocketHandler.broadcastUserLeft(saloonId, userId, connectedCount);
    }

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

    public Optional<ActiveSessionDTO> getActiveSession(Long userId) {
        return redisService.getActiveSession(userId);
    }

    private boolean isPremium(User user) {
        return false;
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

        return new JoinResponseDTO(
                saloon.getId(),
                saloon.getName(),
                activeSession.getJoinedAt(),
                activeSession.getEndsAt(),
                connectedCount);
    }
}

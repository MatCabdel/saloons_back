package com.backend_project_template.domains.presence;

import com.backend_project_template.domains.presence.dto.PresenceDTO;
import com.backend_project_template.domains.presence.dto.UserPresenceDTO;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.session.SessionRedisService;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import com.backend_project_template.domains.user.UserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service pour la gestion de la présence et la recherche géographique de
 * saloons.
 */
@Service
@SuppressWarnings({ "checkstyle:ParameterNumber", "checkstyle:MagicNumber" })
public class PresenceService {

    /** Nombre de mètres par degré de latitude. */
    private static final double METERS_PER_DEGREE = 111000.0;
    /** Rayon de la Terre en mètres. */
    private static final int EARTH_RADIUS_METERS = 6371000;
    /** Diviseur pour formule Haversine. */
    private static final double HAVERSINE_DIVISOR = 2.0;

    private final SessionRedisService redisService;
    private final SaloonRepository saloonRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public PresenceService(SessionRedisService redisService,
            SaloonRepository saloonRepository,
            UserRepository userRepository,
            UserService userService) {
        this.redisService = redisService;
        this.saloonRepository = saloonRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Récupère les informations de présence d'un saloon.
     */
    public PresenceDTO getSaloonPresence(Long saloonId) {
        Saloon saloon = saloonRepository.findById(saloonId)
                .orElseThrow(() -> new RuntimeException("Saloon non trouvé"));

        Set<String> userIds = redisService.getPresenceUserIds(saloonId);
        List<UserPresenceDTO> connectedUsers = new ArrayList<>();

        for (String userIdStr : userIds) {
            Long userId = Long.parseLong(userIdStr);
            UserPresenceDTO userPresence = getUserPresenceInfo(userId);
            if (userPresence != null) {
                connectedUsers.add(userPresence);
            }
        }

        return new PresenceDTO(
                saloonId,
                saloon.getName(),
                connectedUsers.size(),
                connectedUsers);
    }

    /**
     * Récupère uniquement le nombre d'utilisateurs connectés à un saloon.
     */
    public int getPresenceCount(Long saloonId) {
        return redisService.getPresenceCount(saloonId);
    }

    /**
     * Récupère tous les compteurs de présence de tous les saloons.
     */
    public Map<Long, Integer> getAllPresenceCounts() {
        return redisService.getAllPresenceCounts();
    }

    /**
     * Récupère les infos de présence d'un utilisateur (depuis cache ou DB).
     */
    private UserPresenceDTO getUserPresenceInfo(Long userId) {
        // Essayer le cache Redis d'abord
        Optional<Map<String, String>> cached = redisService.getCachedUserInfo(userId);

        if (cached.isPresent()) {
            Map<String, String> data = cached.get();
            String ageStr = data.get("age");
            Integer age = (ageStr != null && !ageStr.isEmpty()) ? Integer.parseInt(ageStr) : null;
            String city = data.get("city");
            LocalDateTime profileImageUpdatedAt = parseProfileImageUpdatedAt(data.get("profileImageUpdatedAt"));
            return new UserPresenceDTO(
                    userId,
                    data.get("userName"),
                    data.get("imgUrl"),
                    profileImageUpdatedAt,
                    age,
                    (city != null && !city.isEmpty()) ? city : null);
        }

        // Sinon, charger depuis la DB et mettre en cache
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        Integer age = userService.calculateAge(user.getBirthDate());
        String city = user.getCity();
        redisService.cacheUserInfo(
                userId,
                user.getUserName(),
                user.getImgUrl(),
                user.getProfileImageUpdatedAt(),
                age,
                city);

        return new UserPresenceDTO(
                userId,
                user.getUserName(),
                user.getImgUrl(),
                user.getProfileImageUpdatedAt(),
                age,
                city);
    }

    private LocalDateTime parseProfileImageUpdatedAt(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Recherche les saloons dans un rayon donné (en mètres).
     */
    public List<SaloonMapDTO> getNearbySaloons(double lat, double lng, int radiusMeters, boolean includePrivate) {
        // Calculer la bounding box approximative
        double latDelta = radiusMeters / METERS_PER_DEGREE;
        double lngDelta = radiusMeters / (METERS_PER_DEGREE * Math.cos(Math.toRadians(lat)));

        BigDecimal minLat = BigDecimal.valueOf(lat - latDelta);
        BigDecimal maxLat = BigDecimal.valueOf(lat + latDelta);
        BigDecimal minLng = BigDecimal.valueOf(lng - lngDelta);
        BigDecimal maxLng = BigDecimal.valueOf(lng + lngDelta);

        List<Saloon> saloons = includePrivate
                ? saloonRepository.findByBoundingBox(minLat, maxLat, minLng, maxLng)
                : saloonRepository.findPublicByBoundingBox(minLat, maxLat, minLng, maxLng);

        List<SaloonMapDTO> result = new ArrayList<>();
        for (Saloon saloon : saloons) {
            double distance = calculateDistance(
                    lat, lng,
                    saloon.getLatitude().doubleValue(),
                    saloon.getLongitude().doubleValue());

            // Filtrer par distance exacte
            if (distance <= radiusMeters) {
                int connectedCount = redisService.getPresenceCount(saloon.getId());
                result.add(new SaloonMapDTO(saloon, (int) distance, connectedCount));
            }
        }

        return result;
    }

    /**
     * Recherche les saloons dans une bounding box.
     */
    public List<SaloonMapDTO> getSaloonsInBbox(double minLat, double maxLat,
            double minLng, double maxLng, boolean includePrivate) {
        List<Saloon> saloons = includePrivate
                ? saloonRepository.findByBoundingBox(
                        BigDecimal.valueOf(minLat),
                        BigDecimal.valueOf(maxLat),
                        BigDecimal.valueOf(minLng),
                        BigDecimal.valueOf(maxLng))
                : saloonRepository.findPublicByBoundingBox(
                        BigDecimal.valueOf(minLat),
                        BigDecimal.valueOf(maxLat),
                        BigDecimal.valueOf(minLng),
                        BigDecimal.valueOf(maxLng));

        List<SaloonMapDTO> result = new ArrayList<>();
        for (Saloon saloon : saloons) {
            int connectedCount = redisService.getPresenceCount(saloon.getId());
            result.add(new SaloonMapDTO(saloon, null, connectedCount));
        }

        return result;
    }

    /**
     * Calcule la distance en mètres entre deux points GPS (formule Haversine).
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / HAVERSINE_DIVISOR) * Math.sin(dLat / HAVERSINE_DIVISOR)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / HAVERSINE_DIVISOR) * Math.sin(dLng / HAVERSINE_DIVISOR);

        double c = HAVERSINE_DIVISOR * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}

package com.backend_project_template.domains.presence;

import com.backend_project_template.domains.presence.dto.JoinRequestDTO;
import com.backend_project_template.domains.presence.dto.JoinResponseDTO;
import com.backend_project_template.domains.presence.dto.PresenceDTO;
import com.backend_project_template.domains.session.SaloonSessionService;
import com.backend_project_template.domains.session.SessionException;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST pour la gestion de la présence et des sessions.
 */
@RestController
@RequestMapping("/api/saloons")
@SuppressWarnings("checkstyle:ParameterNumber")
public class PresenceController {

    private final SaloonSessionService sessionService;
    private final PresenceService presenceService;
    private final UserRepository userRepository;

    public PresenceController(SaloonSessionService sessionService,
            PresenceService presenceService,
            UserRepository userRepository) {
        this.sessionService = sessionService;
        this.presenceService = presenceService;
        this.userRepository = userRepository;
    }

    /**
     * Rejoindre un saloon.
     * POST /api/saloons/{saloonId}/join
     */
    @PostMapping("/{saloonId}/join")
    public ResponseEntity<?> joinSaloon(
            @PathVariable Long saloonId,
            @RequestBody(required = false) JoinRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long userId = getUserId(userDetails);
            Double lat = request != null ? request.getLat() : null;
            Double lng = request != null ? request.getLng() : null;

            JoinResponseDTO response = sessionService.joinSaloon(userId, saloonId, lat, lng);
            return ResponseEntity.ok(response);
        } catch (SessionException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Quitter un saloon.
     * POST /api/saloons/{saloonId}/leave
     */
    @PostMapping("/{saloonId}/leave")
    public ResponseEntity<?> leaveSaloon(
            @PathVariable Long saloonId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long userId = getUserId(userDetails);
            sessionService.leaveSaloon(userId, saloonId);
            return ResponseEntity.ok(Map.of("message", "Vous avez quitté le saloon"));
        } catch (SessionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupérer la présence d'un saloon (liste des utilisateurs connectés).
     * GET /api/saloons/{saloonId}/presence
     */
    @GetMapping("/{saloonId}/presence")
    public ResponseEntity<PresenceDTO> getPresence(@PathVariable Long saloonId) {
        PresenceDTO presence = presenceService.getSaloonPresence(saloonId);
        return ResponseEntity.ok(presence);
    }

    /**
     * Récupérer les saloons à proximité.
     * GET /api/saloons/nearby?lat=X&lng=Y&radius=Z
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<SaloonMapDTO>> getNearbySaloons(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5000") int radius) {

        List<SaloonMapDTO> saloons = presenceService.getNearbySaloons(lat, lng, radius);
        return ResponseEntity.ok(saloons);
    }

    /**
     * Récupérer les saloons dans une bounding box.
     * GET /api/saloons/bbox?minLat=X&maxLat=Y&minLng=Z&maxLng=W
     */
    @GetMapping("/bbox")
    public ResponseEntity<List<SaloonMapDTO>> getSaloonsInBbox(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLng,
            @RequestParam double maxLng) {

        List<SaloonMapDTO> saloons = presenceService.getSaloonsInBbox(minLat, maxLat, minLng, maxLng);
        return ResponseEntity.ok(saloons);
    }

    /**
     * Récupère l'ID de l'utilisateur à partir du UserDetails.
     */
    private Long getUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return user.getId();
    }
}

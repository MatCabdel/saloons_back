package com.backend_project_template.domains.presence;

import com.backend_project_template.domains.presence.dto.ActiveSessionDTO;
import com.backend_project_template.domains.session.SaloonSessionService;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controller REST pour les informations de session utilisateur.
 */
@RestController
@RequestMapping("/api/users")
public class UserSessionController {

    private final SaloonSessionService sessionService;
    private final UserRepository userRepository;

    public UserSessionController(SaloonSessionService sessionService,
            UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    /**
     * Récupérer la session active de l'utilisateur connecté.
     * GET /api/users/me/session
     */
    @GetMapping("/me/session")
    public ResponseEntity<?> getMySession(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Optional<ActiveSessionDTO> session = sessionService.getActiveSession(userId);

        if (session.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "hasActiveSession", false,
                    "message", "Aucune session active"));
        }

        return ResponseEntity.ok(Map.of(
                "hasActiveSession", true,
                "session", session.get()));
    }

    /**
     * Forcer la déconnexion de l'utilisateur (quitter le saloon actuel).
     * POST /api/users/me/session/leave
     */
    @PostMapping("/me/session/leave")
    public ResponseEntity<?> leaveCurrentSession(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        sessionService.forceLeave(userId);
        return ResponseEntity.ok(Map.of("message", "Session terminée"));
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

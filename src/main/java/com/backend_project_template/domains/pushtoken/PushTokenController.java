package com.backend_project_template.domains.pushtoken;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller pour la gestion des tokens push FCM.
 */
@RestController
@RequestMapping("/push-tokens")
public class PushTokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushTokenController.class);

    private final PushTokenService pushTokenService;
    private final UserRepository userRepository;

    public PushTokenController(PushTokenService pushTokenService, UserRepository userRepository) {
        this.pushTokenService = pushTokenService;
        this.userRepository = userRepository;
    }

    /**
     * Enregistre ou met à jour un token FCM pour l'utilisateur connecté.
     * POST /push-tokens
     */
    @PostMapping
    public ResponseEntity<Void> registerToken(
            @Valid @RequestBody PushTokenRequest request,
            Principal principal) {

        if (principal == null) {
            LOGGER.warn("📱 Attempt to register token without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElse(null);

        if (user == null) {
            LOGGER.warn("📱 User not found for email: {}", principal.getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            pushTokenService.registerToken(request.token(), request.platform(), user);
            LOGGER.info("📱 Token registered for user {} on {}", user.getId(), request.platform());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            LOGGER.error("📱 Failed to register token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Désactive un token FCM (appelé lors du logout).
     * DELETE /push-tokens/{token}
     */
    @DeleteMapping("/{token}")
    public ResponseEntity<Void> unregisterToken(
            @PathVariable String token,
            Principal principal) {

        if (principal == null) {
            LOGGER.warn("📱 Attempt to unregister token without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            pushTokenService.deactivateToken(token);
            LOGGER.info("📱 Token deactivated");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            LOGGER.error("📱 Failed to deactivate token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

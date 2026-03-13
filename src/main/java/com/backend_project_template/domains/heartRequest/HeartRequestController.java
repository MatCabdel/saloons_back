package com.backend_project_template.domains.heartRequest;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/heart-requests")
public class HeartRequestController {

    private final HeartRequestService heartRequestService;
    private final UserRepository userRepository;

    public HeartRequestController(HeartRequestService heartRequestService, UserRepository userRepository) {
        this.heartRequestService = heartRequestService;
        this.userRepository = userRepository;
    }

    /**
     * Envoie un coup de cœur à un autre utilisateur.
     */
    @PostMapping
    public ResponseEntity<?> sendHeartRequest(@RequestBody SendHeartRequestDTO requestDTO, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User sender = userRepository.findByEmail(principal.getName())
                .orElse(null);

        if (sender == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            HeartRequestDTO result = heartRequestService.sendHeartRequest(
                    sender.getId(),
                    requestDTO.conversationId(),
                    requestDTO.receiverId(),
                    requestDTO.saloonId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Récupère le statut des coups de cœur pour une conversation.
     */
    @GetMapping("/status/{conversationId}")
    public ResponseEntity<?> getHeartRequestStatus(@PathVariable Long conversationId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            HeartRequestStatusDTO status = heartRequestService.getHeartRequestStatus(user.getId(), conversationId);
            return ResponseEntity.ok(status);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Récupère les coups de cœur envoyés par l'utilisateur.
     */
    @GetMapping("/sent")
    public ResponseEntity<?> getSentHeartRequests(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<HeartRequestDTO> requests = heartRequestService.getSentHeartRequests(user.getId());
        return ResponseEntity.ok(requests);
    }

    /**
     * Récupère les coups de cœur reçus par l'utilisateur.
     */
    @GetMapping("/received")
    public ResponseEntity<?> getReceivedHeartRequests(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<HeartRequestDTO> requests = heartRequestService.getReceivedHeartRequests(user.getId());
        return ResponseEntity.ok(requests);
    }
}

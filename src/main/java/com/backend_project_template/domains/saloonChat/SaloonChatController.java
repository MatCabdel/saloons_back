package com.backend_project_template.domains.saloonChat;

import com.backend_project_template.domains.presence.dto.ActiveSessionDTO;
import com.backend_project_template.domains.review.ReviewDemoService;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.session.SessionRedisService;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/saloon-chat")
public class SaloonChatController {

    private static final int HTTP_FORBIDDEN = 403;
    private static final int CHAT_ACTIVATION_THRESHOLD = 3;

    @Autowired
    private SaloonChatService chatService;

    @Autowired
    private SessionRedisService sessionRedisService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SaloonRepository saloonRepository;

    @Autowired
    private ReviewDemoService reviewDemoService;

    /**
     * Récupère l'historique du chat avec les messages depuis le joinedAt de
     * l'utilisateur
     * C'est l'endpoint principal à utiliser pour charger le chat
     */
    @GetMapping("/{saloonId}/history")
    public ResponseEntity<SaloonChatHistoryDTO> getChatHistory(
            @PathVariable Long saloonId,
            @RequestParam(defaultValue = "50") int limit,
            Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Récupérer la session active de l'utilisateur
        Optional<ActiveSessionDTO> sessionOpt = sessionRedisService.getActiveSession(user.getId());

        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ActiveSessionDTO session = sessionOpt.get();

        // Vérifier que l'utilisateur est bien dans ce saloon
        if (!session.getSaloonId().equals(saloonId)) {
            return ResponseEntity.badRequest().build();
        }

        Saloon saloon = saloonRepository.findById(saloonId)
                .orElseThrow(() -> new RuntimeException("Saloon not found"));
        boolean reviewDemo = reviewDemoService.isReviewDemo(user, saloon);

        // Récupérer les messages depuis joinedAt
        List<SaloonMessageDTO> messages = chatService.getMessagesSince(saloonId, session.getJoinedAt(), limit);
        if (reviewDemo) {
            messages = reviewDemoService.withDemoMessages(saloonId, messages);
        }

        // Récupérer l'état du chat (basé sur la présence du saloon, pas du chat)
        int connectedCount = sessionRedisService.getPresenceCount(saloonId);
        if (reviewDemo) {
            connectedCount = reviewDemoService.ensureReviewConnectedCount(connectedCount);
        }
        boolean chatEnabled = connectedCount >= CHAT_ACTIVATION_THRESHOLD;

        SaloonChatHistoryDTO response = new SaloonChatHistoryDTO(
                messages,
                session.getJoinedAt(),
                session.getEndsAt(),
                chatEnabled,
                connectedCount);

        return ResponseEntity.ok(response);
    }

    /**
     * Ancien endpoint pour récupérer les messages (pour compatibilité)
     * Sécurisé : vérifie que l'utilisateur a une session active dans ce saloon.
     * 
     * @deprecated Utiliser /history à la place
     */
    @GetMapping("/{saloonId}/messages")
    public ResponseEntity<List<SaloonMessageDTO>> getMessages(
            @PathVariable Long saloonId,
            @RequestParam(defaultValue = "50") int limit,
            Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<ActiveSessionDTO> sessionOpt = sessionRedisService.getActiveSession(user.getId());
        if (sessionOpt.isEmpty() || !sessionOpt.get().getSaloonId().equals(saloonId)) {
            return ResponseEntity.status(HTTP_FORBIDDEN).build();
        }
        return ResponseEntity.ok(chatService.getMessages(saloonId, limit));
    }

    @GetMapping("/{saloonId}/presence")
    public SaloonPresenceDTO getPresence(@PathVariable Long saloonId) {
        int count = sessionRedisService.getPresenceCount(saloonId);
        boolean enabled = count >= CHAT_ACTIVATION_THRESHOLD;
        return new SaloonPresenceDTO(saloonId, count, enabled);
    }

    @PostMapping("/{saloonId}/messages")
    public ResponseEntity<SaloonMessageDTO> sendMessage(
            @PathVariable Long saloonId,
            @RequestBody SendMessageRequest request,
            Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vérifier que l'utilisateur a une session active dans ce saloon
        Optional<ActiveSessionDTO> sessionOpt = sessionRedisService.getActiveSession(user.getId());
        if (sessionOpt.isEmpty() || !sessionOpt.get().getSaloonId().equals(saloonId)) {
            return ResponseEntity.badRequest().build();
        }

        Saloon saloon = saloonRepository.findById(saloonId)
                .orElseThrow(() -> new RuntimeException("Saloon not found"));
        boolean reviewDemo = reviewDemoService.isReviewDemo(user, saloon);

        // Vérifier que le chat est activé (≥3 participants dans le saloon)
        if (!reviewDemo && sessionRedisService.getPresenceCount(saloonId) < CHAT_ACTIVATION_THRESHOLD) {
            return ResponseEntity.status(HTTP_FORBIDDEN).build();
        }

        SaloonMessageDTO message = chatService.sendMessage(saloonId, user.getId(), request.getContent());
        return ResponseEntity.ok(message);
    }

    public static class SendMessageRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}

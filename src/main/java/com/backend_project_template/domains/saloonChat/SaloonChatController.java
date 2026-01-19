package com.backend_project_template.domains.saloonChat;

import com.backend_project_template.domains.presence.dto.ActiveSessionDTO;
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

    @Autowired
    private SaloonChatService chatService;

    @Autowired
    private SaloonPresenceService presenceService;

    @Autowired
    private SessionRedisService sessionRedisService;

    @Autowired
    private UserRepository userRepository;

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

        // Récupérer les messages depuis joinedAt
        List<SaloonMessageDTO> messages = chatService.getMessagesSince(saloonId, session.getJoinedAt(), limit);

        // Récupérer l'état du chat
        int connectedCount = presenceService.getPresence(saloonId);
        boolean chatEnabled = presenceService.isChatEnabled(saloonId);

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
     * 
     * @deprecated Utiliser /history à la place
     */
    @GetMapping("/{saloonId}/messages")
    public List<SaloonMessageDTO> getMessages(
            @PathVariable Long saloonId,
            @RequestParam(defaultValue = "50") int limit) {
        return chatService.getMessages(saloonId, limit);
    }

    @GetMapping("/{saloonId}/presence")
    public SaloonPresenceDTO getPresence(@PathVariable Long saloonId) {
        int count = presenceService.getPresence(saloonId);
        boolean enabled = presenceService.isChatEnabled(saloonId);
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

        // Vérifier que le chat est activé (≥3 participants)
        if (!presenceService.isChatEnabled(saloonId)) {
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

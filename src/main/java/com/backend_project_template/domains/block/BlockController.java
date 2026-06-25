package com.backend_project_template.domains.block;

import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.conversation.ConversationParticipant;
import com.backend_project_template.domains.conversation.ConversationParticipantRepository;
import com.backend_project_template.domains.conversation.ConversationRepository;
import com.backend_project_template.domains.heartRequest.HeartRequestRepository;
import com.backend_project_template.domains.match.MatchService;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SuppressWarnings("checkstyle:ParameterNumber")
public class BlockController {

    private final BlockedUserRepository blockedUserRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final HeartRequestRepository heartRequestRepository;
    private final MatchService matchService;

    public BlockController(
            BlockedUserRepository blockedUserRepository,
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            HeartRequestRepository heartRequestRepository,
            MatchService matchService) {
        this.blockedUserRepository = blockedUserRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.heartRequestRepository = heartRequestRepository;
        this.matchService = matchService;
    }

    /**
     * Bloquer un utilisateur.
     * POST /users/{userId}/block
     */
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long userId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User blocker = userRepository.findByEmail(principal.getName())
                .orElse(null);
        if (blocker == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Empêcher de se bloquer soi-même
        if (blocker.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Vous ne pouvez pas vous bloquer vous-même."));
        }

        User blocked = userRepository.findById(userId).orElse(null);
        if (blocked == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Utilisateur introuvable."));
        }

        // Éviter les doublons
        if (blockedUserRepository.existsByBlockerIdAndBlockedId(blocker.getId(), userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Vous avez déjà bloqué cet utilisateur."));
        }

        // Créer le blocage
        BlockedUser block = new BlockedUser(blocker, blocked);
        blockedUserRepository.save(block);

        // Désactiver la conversation privée existante si elle existe
        conversationRepository.findConversationBetweenUsers(blocker, blocked)
                .ifPresent(conv -> disableConversation(conv, blocker, blocked));

        return ResponseEntity.ok(Map.of("message", "Utilisateur bloqué avec succès."));
    }

    /**
     * Récupère les IDs des utilisateurs bloqués par l'utilisateur courant
     * ainsi que ceux qui l'ont bloqué (blocage mutuel).
     * GET /users/blocked
     */
    @GetMapping("/users/blocked")
    public ResponseEntity<Map<String, Set<Long>>> getBlockedUsers(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Set<Long> mutuallyBlocked = blockedUserRepository.findMutuallyBlockedIds(user.getId());
        return ResponseEntity.ok(Map.of("blockedUserIds", mutuallyBlocked));
    }

    /**
     * Liste tous les blocages (admin).
     * GET /blocks/admin
     */
    @GetMapping("/blocks/admin")
    public ResponseEntity<List<BlockedUserDTO>> getAllBlocks() {
        List<BlockedUserDTO> blocks = blockedUserRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(BlockedUserDTO::new)
                .toList();
        return ResponseEntity.ok(blocks);
    }

    // -------- Logique interne --------

    private void disableConversation(Conversation conversation, User blocker, User blocked) {
        // Soft-delete la participation du bloqueur (marquer comme quitté)
        ConversationParticipant blockerParticipant = conversation.getParticipant(blocker.getId());
        if (blockerParticipant != null && !blockerParticipant.hasLeft()) {
            blockerParticipant.setLeftAt(LocalDateTime.now());
            participantRepository.save(blockerParticipant);
        }

        // Supprimer les HeartRequests liés au bloqueur
        heartRequestRepository.findByConversationId(conversation.getId()).stream()
                .filter(hr -> hr.getSender().getId().equals(blocker.getId())
                        || hr.getReceiver().getId().equals(blocker.getId()))
                .forEach(heartRequestRepository::delete);

        // Quitter le match
        matchService.leaveMatch(blocker, blocked);
    }
}

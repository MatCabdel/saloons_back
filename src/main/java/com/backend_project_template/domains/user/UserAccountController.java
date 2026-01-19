package com.backend_project_template.domains.user;

import com.backend_project_template.domains.auth.FirebaseAuthService;
import com.backend_project_template.domains.conversation.ConversationParticipantRepository;
import com.backend_project_template.domains.match.MatchRepository;
import com.backend_project_template.domains.match.UserLikeRepository;
import com.backend_project_template.domains.message.MessageRepository;
import com.backend_project_template.domains.saloonChat.SaloonMessageRepository;
import com.backend_project_template.domains.saloonSession.SaloonSessionRepository;
import com.backend_project_template.domains.session.SessionRedisService;
import com.backend_project_template.domains.subscription.PremiumSubscriptionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserAccountController {

    private static final int MIN_PASSWORD_LENGTH = 8;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    @Autowired
    private SessionRedisService sessionRedisService;

    @Autowired
    private SaloonSessionRepository saloonSessionRepository;

    @Autowired
    private UserLikeRepository userLikeRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SaloonMessageRepository saloonMessageRepository;

    @Autowired
    private PremiumSubscriptionRepository premiumSubscriptionRepository;

    @Autowired
    private ConversationParticipantRepository conversationParticipantRepository;

    /**
     * Change password for the authenticated user.
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Le mot de passe actuel est incorrect"));
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
    }

    /**
     * Delete the authenticated user's account.
     */
    @DeleteMapping("/delete-account")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Map<String, String>> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Long userId = user.getId();

        // Delete from Firebase Auth
        if (user.getFirebaseUid() != null) {
            firebaseAuthService.deleteUser(user.getFirebaseUid());
        }

        // Clean Redis: session + presence in all saloons
        sessionRedisService.deleteSession(userId);
        sessionRedisService.removeUserFromAllPresence(userId);

        // Delete saloon sessions (history)
        saloonSessionRepository.deleteByUser(user);

        // Detach user from current saloon
        user.setCurrentSaloon(null);
        userRepository.save(user);

        // Delete likes (where user is liker or liked)
        userLikeRepository.deleteByLiker(user);
        userLikeRepository.deleteByLiked(user);

        // Delete matches (where user is user1 or user2)
        matchRepository.deleteByUser1(user);
        matchRepository.deleteByUser2(user);

        // Delete private messages sent by user
        messageRepository.deleteBySender(user);

        // Delete saloon chat messages sent by user
        saloonMessageRepository.deleteBySender(user);

        // Delete premium subscriptions
        premiumSubscriptionRepository.deleteByUser(user);

        // Delete conversation participations
        conversationParticipantRepository.deleteByUserId(userId);

        // Finally, delete the user
        userRepository.delete(user);

        return ResponseEntity.ok(Map.of("message", "Compte supprimé avec succès"));
    }

    /**
     * DTO for change password request.
     */
    public static class ChangePasswordRequest {
        @NotBlank(message = "Le mot de passe actuel est obligatoire")
        private String currentPassword;

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = MIN_PASSWORD_LENGTH, message = "Le nouveau mot de passe doit contenir au moins 8 caractères")
        private String newPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}

package com.backend_project_template.domains.auth;

import com.backend_project_template.domains.user.AuthProvider;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserService;
import com.backend_project_template.service.EmailService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService {

  private final UserService userService;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  @Value("${app.frontend-url:}")
  private String frontendUrl;

  public PasswordResetService(
      UserService userService,
      PasswordResetTokenRepository passwordResetTokenRepository,
      PasswordEncoder passwordEncoder,
      EmailService emailService) {
    this.userService = userService;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
  }

  public void sendResetEmail(String email) {
    var userOpt = userService.findByEmailOptional(email);
    if (userOpt.isEmpty()) {
      return;
    }

    User user = userOpt.get();
    if (user.getAuthProvider() != null && user.getAuthProvider() != AuthProvider.EMAIL) {
      return;
    }

    String token = UUID.randomUUID().toString();
    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.setToken(token);
    resetToken.setUser(user);
    resetToken.setExpiresAt(LocalDateTime.now().plus(1, ChronoUnit.HOURS));
    passwordResetTokenRepository.save(resetToken);

    String baseUrl = frontendUrl != null && !frontendUrl.isBlank() ? frontendUrl : "";
    String resetLink = baseUrl + "/mot-de-passe-oublie?token=" + token;
    String subject = "Réinitialisation de ton mot de passe";
    String html = "<p>Tu as demandé à réinitialiser ton mot de passe.</p>"
        + "<p>Clique sur ce lien pour choisir un nouveau mot de passe :</p>"
        + "<p><a href=\"" + resetLink + "\">" + resetLink + "</a></p>"
        + "<p>Ce lien expire dans 1 heure.</p>";

    emailService.sendEmail(user.getEmail(), subject, html);
  }

  public boolean resetPassword(String token, String newPassword) {
    var tokenOpt = passwordResetTokenRepository.findByToken(token);
    if (tokenOpt.isEmpty()) {
      return false;
    }

    PasswordResetToken resetToken = tokenOpt.get();
    if (resetToken.isUsed() || resetToken.isExpired()) {
      return false;
    }

    User user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(newPassword));
    userService.save(user);

    resetToken.setUsedAt(LocalDateTime.now());
    passwordResetTokenRepository.save(resetToken);
    return true;
  }
}

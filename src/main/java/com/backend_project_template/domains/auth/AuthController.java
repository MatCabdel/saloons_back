package com.backend_project_template.domains.auth;

import com.backend_project_template.domains.auth.dto.ForgotPasswordRequest;
import com.backend_project_template.domains.auth.dto.ResetPasswordRequest;
import com.backend_project_template.domains.auth.dto.UserLoginDTO;
import com.backend_project_template.domains.auth.dto.UserLoginResponseDTO;
import com.backend_project_template.domains.auth.dto.UserRegistrationDTO;
import com.backend_project_template.domains.auth.dto.UserRegistrationResponseDTO;
import com.backend_project_template.domains.user.AuthProvider;
import com.backend_project_template.domains.user.ProfileStatus;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserDTO;
import com.backend_project_template.domains.user.UserService;
import com.backend_project_template.security.AuthenticationService;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final UserService userService;
  private final AuthenticationService authenticationService;
  private final FirebaseAuthService firebaseAuthService;
  private final PasswordResetService passwordResetService;

  public AuthController(
      UserService userService,
      AuthenticationService authenticationService,
      FirebaseAuthService firebaseAuthService,
      PasswordResetService passwordResetService) {
    this.userService = userService;
    this.authenticationService = authenticationService;
    this.firebaseAuthService = firebaseAuthService;
    this.passwordResetService = passwordResetService;
  }

  @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserRegistrationResponseDTO> register(
      @Valid @ModelAttribute UserRegistrationDTO dto) {
    User u = userService.registerUserWithImage(dto, Set.of("ROLE_USER"));
    UserRegistrationResponseDTO body = new UserRegistrationResponseDTO();
    body.setId(u.getId());
    body.setEmail(u.getEmail());
    body.setUserName(u.getUserName());
    body.setImgUrl(u.getImgUrl());
    body.setProfileImageUpdatedAt(u.getProfileImageUpdatedAt());
    body.setDescription(u.getDescription());
    body.setCity(u.getCity());

    return ResponseEntity.status(HttpStatus.CREATED).body(body);
  }

  @PostMapping("/login")
  public ResponseEntity<UserLoginResponseDTO> authenticate(@RequestBody UserLoginDTO userLoginDTO) {
    User existingUser = userService.findByEmailOptional(userLoginDTO.getEmail()).orElse(null);
    if (existingUser != null && !existingUser.isEnabled()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    String token = authenticationService.authenticate(
        userLoginDTO.getEmail(), userLoginDTO.getPassword());
    User user = userService.findByEmail(userLoginDTO.getEmail());

    // Mettre à jour la date de dernière connexion
    user.setLastLoginAt(LocalDateTime.now());
    userService.save(user);

    UserLoginResponseDTO response = new UserLoginResponseDTO();
    response.setId(user.getId());
    response.setEmail(user.getEmail());
    response.setUserName(user.getUserName());
    response.setRole(user.getRoles().stream().findFirst().orElse(null));
    response.setImgUrl(user.getImgUrl());
    response.setProfileImageUpdatedAt(user.getProfileImageUpdatedAt());
    response.setToken(token);
    response.setAuthProvider(user.getAuthProvider() != null ? user.getAuthProvider().name() : null);
    response.setFirstname(user.getFirstName());
    response.setLastname(user.getLastName());
    response.setIsPremium(user.getIsPremium());
    response.setCity(user.getCity());
    response.setDescription(user.getDescription());
    response.setBirthDate(user.getBirthDate());
    response.setAge(userService.calculateAge(user.getBirthDate()));
    // Pour les utilisateurs existants sans profileStatus, considérer comme ACTIVE
    // si ils ont déjà un userName (profil déjà complété)
    if (user.getProfileStatus() != null) {
      response.setProfileStatus(user.getProfileStatus().name());
    } else if (user.getUserName() != null && !user.getUserName().isEmpty()) {
      response.setProfileStatus(ProfileStatus.ACTIVE.name());
    } else {
      response.setProfileStatus(ProfileStatus.PROFILE_INCOMPLETE.name());
    }

    return ResponseEntity.ok(response);
  }

  /**
   * Authenticate or register a user via Firebase (Google/Facebook/Apple).
   */
  @PostMapping("/firebase")
  public ResponseEntity<AuthResponse> authenticateWithFirebase(
      @Valid @RequestBody FirebaseAuthRequest request) {
    FirebaseToken firebaseToken = firebaseAuthService.verifyToken(request.getFirebaseToken());
    if (firebaseToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = firebaseAuthService.getEmailFromToken(firebaseToken);
    String firebaseUid = firebaseAuthService.getUidFromToken(firebaseToken);
    String displayName = firebaseAuthService.getNameFromToken(firebaseToken);
    String photoUrl = firebaseAuthService.getPictureFromToken(firebaseToken);
    String provider = firebaseAuthService.getProviderFromToken(firebaseToken);

    AuthProvider authProvider = mapFirebaseProvider(provider);
    boolean isNewUser = false;

    User user = userService.findByEmailOptional(email).orElse(null);

    if (user == null) {
      // Create new user
      user = userService.createFirebaseUser(email, firebaseUid, displayName, photoUrl, authProvider);
      isNewUser = true;
    } else {
      if (!user.isEnabled()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

      // Update Firebase UID if not set
      if (user.getFirebaseUid() == null) {
        user.setFirebaseUid(firebaseUid);
        user.setAuthProvider(authProvider);
      }
    }

    // Mettre à jour la date de dernière connexion
    user.setLastLoginAt(LocalDateTime.now());
    user = userService.save(user);

    String jwtToken = authenticationService.generateTokenForUser(user);
    UserDTO userDTO = new UserDTO(user);

    return ResponseEntity.ok(new AuthResponse(userDTO, jwtToken, isNewUser));
  }

  /**
   * Register a new user with email and password.
   */
  @PostMapping("/register-email")
  public ResponseEntity<AuthResponse> registerWithEmail(
      @Valid @RequestBody EmailRegisterRequest request) {
    // Check if email already exists
    if (userService.existsByEmail(request.getEmail())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    User user = userService.createEmailUser(
        request.getEmail(),
        request.getPassword(),
        request.getFirstName(),
        request.getLastName());

    // Registration returns a JWT immediately, so it also counts as a successful login.
    user.setLastLoginAt(LocalDateTime.now());
    user = userService.save(user);

    String jwtToken = authenticationService.generateTokenForUser(user);
    UserDTO userDTO = new UserDTO(user);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new AuthResponse(userDTO, jwtToken, true));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    passwordResetService.sendResetEmail(request.getEmail());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    boolean reset = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
    if (!reset) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    return ResponseEntity.ok().build();
  }

  private AuthProvider mapFirebaseProvider(String provider) {
    if (provider == null) {
      return AuthProvider.EMAIL;
    }
    return switch (provider) {
      case "google.com" -> AuthProvider.GOOGLE;
      case "facebook.com" -> AuthProvider.FACEBOOK;
      case "apple.com" -> AuthProvider.APPLE;
      default -> AuthProvider.EMAIL;
    };
  }
}

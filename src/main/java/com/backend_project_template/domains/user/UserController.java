package com.backend_project_template.domains.user;

import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.saloonSession.SaloonSessionRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SaloonRepository saloonRepository;

  @Autowired
  private SaloonSessionRepository saloonSessionRepository;

  @GetMapping("/{email}")
  public ResponseEntity<UserDTO> getUserProfile(@PathVariable String email,
      @AuthenticationPrincipal UserDetails userDetails) {
    if (!Objects.equals(userDetails.getUsername(), email)) {
      throw new AccessDeniedException("Access denied");
    }
    User user = userService.findByEmail(email);
    UserDTO dto = new UserDTO(user);
    dto.setAge(userService.calculateAge(user.getBirthDate()));
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/profile/{id}")
  public ResponseEntity<UserDTO> getUserProfile(@PathVariable Long id) {
    User targetUser = userService.findById(id);
    int age = userService.calculateAge(targetUser.getBirthDate());
    UserDTO dto = new UserDTO(targetUser);
    dto.setAge(age);
    return ResponseEntity.ok(dto);
  }

  @GetMapping
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    List<User> users = userRepository.findAll();
    if (users.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    List<UserDTO> dtos = users
        .stream()
        .map(user -> {
          UserDTO dto = new UserDTO(user);
          dto.setAge(userService.calculateAge(user.getBirthDate()));
          return dto;
        })
        .toList();
    return ResponseEntity.ok(dtos);
  }

  @PatchMapping("/{userId}/connect-saloon/{saloonId}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('REVIEWER')")
  public ResponseEntity<UserDTO> connectUserToSaloon(@PathVariable Long userId, @PathVariable Long saloonId) {
    UserDTO dto = userService.connectUserToSaloon(userId, saloonId);
    return ResponseEntity.ok(dto);
  }

  @PatchMapping("/{userId}/disconnect-saloon")
  public ResponseEntity<UserDTO> disconnectUserFromSaloon(@PathVariable Long userId) {
    User user = userService.findById(userId);
    user.setCurrentSaloon(null);
    userRepository.save(user);
    UserDTO dto = new UserDTO(user);
    return ResponseEntity.ok(dto);
  }

  @PatchMapping("/{userId}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('REVIEWER')")
  public ResponseEntity<UserDTO> updateUserProfile(
      @PathVariable Long userId,
      @Valid @RequestBody UserProfileUpdateRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    User user = userService.findById(userId);
    if (userDetails == null || !Objects.equals(userDetails.getUsername(), user.getEmail())) {
      throw new AccessDeniedException("Access denied");
    }
    UserDTO dto = userService.updateUserProfile(user, request);
    return ResponseEntity.ok(dto);
  }

  /**
   * Complete user profile during onboarding.
   * Only accessible by the authenticated user for their own profile.
   */
  @PutMapping("/complete-profile")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('REVIEWER')")
  public ResponseEntity<UserDTO> completeProfile(
      @Valid @RequestBody CompleteProfileRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    User user = userService.findByEmail(userDetails.getUsername());
    User updatedUser = userService.completeProfile(user, request);
    return ResponseEntity.ok(new UserDTO(updatedUser));
  }

  /**
   * Upload profile image during onboarding.
   */
  @PostMapping("/upload-profile-image")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('REVIEWER')")
  public ResponseEntity<UserDTO> uploadProfileImage(
      @RequestParam("image") org.springframework.web.multipart.MultipartFile image,
      @AuthenticationPrincipal UserDetails userDetails) {
    User user = userService.findByEmail(userDetails.getUsername());
    String imageUrl = userService.saveUserImage(user, image);
    user.setImgUrl(imageUrl);
    User savedUser = userService.save(user);
    return ResponseEntity.ok(new UserDTO(savedUser));
  }
}

package com.backend_project_template.domains.user;

import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.saloonSession.SaloonSessionRepository;
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
  public ResponseEntity<UserDTO> getUserProfile(@PathVariable String email, @AuthenticationPrincipal UserDetails userDetails) {
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
    User user = userService.findById(id);
    int age = userService.calculateAge(user.getBirthDate());
    UserDTO dto = new UserDTO(user);
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
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
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
  /*
   * @PatchMapping("/{userId}/update-profile")
   * public ResponseEntity<UserDTO> updateUserProfile(
   *
   * @PathVariable Long userId,
   *
   * @RequestParam(required = false) String description,
   *
   * @RequestParam(required = false) String city,
   *
   * @RequestParam(required = false) MultipartFile image) {
   * User user = userService.findById(userId);
   *
   * if (description != null) {
   * user.setDescription(description);
   * }
   * if (city != null) {
   * user.setCity(city);
   * }
   * if (image != null && !image.isEmpty()) {
   * String imgUrl = userService.saveUserImage(user, image);
   * user.setImgUrl(imgUrl);
   * }
   *
   * userRepository.save(user);
   * UserDTO dto = new UserDTO(user);
   * dto.setAge(userService.calculateAge(user.getBirthDate()));
   * return ResponseEntity.ok(dto);
   * }
   */
}

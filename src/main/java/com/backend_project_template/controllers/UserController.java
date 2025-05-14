package com.backend_project_template.controllers;

import com.backend_project_template.Entity.User;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.user.UserDTO;
import com.backend_project_template.repository.UserRepository;
import com.backend_project_template.service.UserService;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class UserController {

  private final UserService userService;
  private final UserRepository userRepository;
  private final SaloonRepository saloonRepository;

  public UserController(UserService userService, UserRepository userRepository, SaloonRepository saloonRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.saloonRepository = saloonRepository;
  }

  @GetMapping("/{email}")
  public ResponseEntity<User> getUserProfile(@PathVariable String email, @AuthenticationPrincipal UserDetails userDetails) {
    if (!Objects.equals(userDetails.getUsername(), email)) {
      throw new AccessDeniedException("Access denied");
    }
    User user = userService.findByEmail(email);
    return ResponseEntity.ok(user);
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
    List<UserDTO> dtos = users.stream().map(user -> {
      UserDTO dto = new UserDTO(user);
      dto.setAge(userService.calculateAge(user.getBirthDate()));
      return dto;
    }).toList();
    return ResponseEntity.ok(dtos);
  }

  @PatchMapping("/{userId}/connect-saloon/{saloonId}")
  public ResponseEntity<UserDTO> connectUserToSaloon(@PathVariable Long userId, @PathVariable Long saloonId) {
    User user = userService.findById(userId);
    Saloon saloon = saloonRepository.findById(saloonId).orElseThrow();
    user.setCurrentSaloon(saloon);
    userRepository.save(user);
    UserDTO dto = new UserDTO(user);
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
}

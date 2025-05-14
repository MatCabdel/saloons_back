package com.backend_project_template.controllers;

import com.backend_project_template.Entity.User;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.saloonSession.SaloonSession;
import com.backend_project_template.domains.saloonSession.SaloonSessionDTO;
import com.backend_project_template.domains.saloonSession.SaloonSessionRepository;
import com.backend_project_template.domains.user.UserDTO;
import com.backend_project_template.repository.UserRepository;
import com.backend_project_template.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
  public ResponseEntity<SaloonSessionDTO> connectUserToSaloon(@PathVariable Long userId, @PathVariable Long saloonId) {
    User user = userService.findById(userId);
    Saloon saloon = saloonRepository.findById(saloonId).orElseThrow();

    SaloonSession session = new SaloonSession();
    session.setUser(user);
    session.setSaloon(saloon);
    session.setConnectedAt(LocalDateTime.now());
    session.setDisconnectedAt(null);
    saloonSessionRepository.save(session);

    user.setCurrentSaloon(saloon);
    userRepository.save(user);

    return ResponseEntity.ok(new SaloonSessionDTO(session));
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

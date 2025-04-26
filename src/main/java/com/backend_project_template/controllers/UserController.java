package com.backend_project_template.controllers;

import com.backend_project_template.Entity.User;
import com.backend_project_template.repository.UserRepository;
import com.backend_project_template.service.UserService;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class UserController {

  private final UserService userService;
  private final UserRepository userRepository;

  public UserController(UserService userService, UserRepository userRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
  }

  @GetMapping("/{email}")
  public ResponseEntity<User> getUserProfile(@PathVariable String email, @AuthenticationPrincipal UserDetails userDetails) {
    if (!Objects.equals(userDetails.getUsername(), email)) {
      throw new AccessDeniedException("Access denied");
    }
    User user = userService.findByEmail(email);
    return ResponseEntity.ok(user);
  }

  @GetMapping
  public ResponseEntity<List<User>> getAllUsers() {
    List<User> users = userRepository.findAll();
    if (users.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(users);
  }
}

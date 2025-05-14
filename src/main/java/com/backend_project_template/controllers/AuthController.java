package com.backend_project_template.controllers;

import com.backend_project_template.Entity.User;
import com.backend_project_template.dto.UserLoginDTO;
import com.backend_project_template.dto.UserLoginResponseDTO;
import com.backend_project_template.dto.UserRegistrationDTO;
import com.backend_project_template.security.AuthenticationService;
import com.backend_project_template.service.UserService;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final UserService userService;
  private final AuthenticationService authenticationService;

  public AuthController(UserService userService, AuthenticationService authenticationService) {
    this.userService = userService;
    this.authenticationService = authenticationService;
  }

  @PostMapping(value = "/register", consumes = { "multipart/form-data" })
  public ResponseEntity<User> register(@ModelAttribute UserRegistrationDTO registrationDTO) {
    User registerUser = userService.registerUserWithImage(registrationDTO, Set.of("ROLE_USER"));
    return ResponseEntity.status(HttpStatus.CREATED).body(registerUser);
  }

  @PostMapping("/login")
  public ResponseEntity<UserLoginResponseDTO> authenticate(@RequestBody UserLoginDTO userLoginDTO) {
    String token = authenticationService.authenticate(userLoginDTO.getEmail(), userLoginDTO.getPassword());
    User user = userService.findByEmail(userLoginDTO.getEmail());
    UserLoginResponseDTO response = new UserLoginResponseDTO();
    response.setId(user.getId());
    response.setEmail(user.getEmail());
    response.setUserName(user.getUserName());
    response.setRole(user.getRoles().stream().findFirst().orElse(null));
    response.setImgUrl(user.getImgUrl());
    response.setToken(token);
    return ResponseEntity.ok(response);
  }
}

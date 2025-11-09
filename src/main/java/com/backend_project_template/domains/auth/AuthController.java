package com.backend_project_template.domains.auth;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.auth.dto.UserLoginDTO;
import com.backend_project_template.domains.auth.dto.UserLoginResponseDTO;
import com.backend_project_template.domains.auth.dto.UserRegistrationDTO;
import com.backend_project_template.domains.auth.dto.UserRegistrationResponseDTO;
import com.backend_project_template.security.AuthenticationService;
import com.backend_project_template.domains.user.UserService;
import jakarta.validation.Valid;
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

  public AuthController(UserService userService, AuthenticationService authenticationService) {
    this.userService = userService;
    this.authenticationService = authenticationService;
  }

  @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserRegistrationResponseDTO> register(@Valid @ModelAttribute UserRegistrationDTO dto) {
    User u = userService.registerUserWithImage(dto, Set.of("ROLE_USER"));
    UserRegistrationResponseDTO body = new UserRegistrationResponseDTO();
    body.setId(u.getId());
    body.setEmail(u.getEmail());
    body.setUserName(u.getUserName());
    body.setImgUrl(u.getImgUrl());
    body.setDescription(u.getDescription());
    body.setCity(u.getCity());

    return ResponseEntity.status(HttpStatus.CREATED).body(body);
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
    response.setCity(user.getCity());
    response.setDescription(user.getDescription());
    response.setBirthDate(user.getBirthDate());
    response.setAge(userService.calculateAge(user.getBirthDate()));

    return ResponseEntity.ok(response);
  }
}

package com.backend_project_template.unit.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend_project_template.domains.auth.AuthController;
import com.backend_project_template.domains.auth.AuthResponse;
import com.backend_project_template.domains.auth.EmailRegisterRequest;
import com.backend_project_template.domains.auth.FirebaseAuthService;
import com.backend_project_template.domains.auth.PasswordResetService;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserService;
import com.backend_project_template.security.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private UserService userService;

  @Mock
  private AuthenticationService authenticationService;

  @Mock
  private FirebaseAuthService firebaseAuthService;

  @Mock
  private PasswordResetService passwordResetService;

  @InjectMocks
  private AuthController authController;

  @Test
  void emailRegistrationRecordsTheInitialLogin() {
    EmailRegisterRequest request = new EmailRegisterRequest();
    request.setEmail("user@example.com");
    request.setPassword("password123");
    request.setFirstName("Test");
    request.setLastName("User");

    User user = new User();
    user.setEmail(request.getEmail());

    when(userService.createEmailUser(
        request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName()))
        .thenReturn(user);
    when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(authenticationService.generateTokenForUser(user)).thenReturn("jwt-token");

    ResponseEntity<AuthResponse> response = authController.registerWithEmail(request);

    assertNotNull(user.getLastLoginAt());
    assertNotNull(response.getBody());
    assertEquals(user.getLastLoginAt(), response.getBody().getUser().getLastLoginAt());
    verify(userService).save(user);
  }
}

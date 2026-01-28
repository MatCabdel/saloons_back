package com.backend_project_template.security;

import com.backend_project_template.domains.user.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthenticationService(JwtService jwtService, AuthenticationManager authenticationManager) {
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
  }

  public String authenticate(String email, String password) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(email, password));
    return jwtService.generateToken((UserDetails) authentication.getPrincipal());
  }

  /**
   * Generate a JWT token for a user without password authentication (Firebase
   * auth).
   */
  public String generateTokenForUser(User user) {
    return jwtService.generateToken(user);
  }
}

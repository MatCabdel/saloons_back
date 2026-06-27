package com.backend_project_template.security;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final int LAST_LOGIN_REFRESH_HOURS = 1;
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final UserRepository userRepository;

  public JwtAuthenticationFilter(
      JwtService jwtService,
      UserDetailsService userDetailsService,
      UserRepository userRepository) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
      String jwt = authHeader.substring(BEARER_PREFIX.length());

      try {
        String username = jwtService.extractClaims(jwt).getSubject();

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);
          if (jwtService.extractClaims(jwt).getExpiration().after(new Date())) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            refreshLastLoginAt(userDetails);
          }
        }
      } catch (UsernameNotFoundException e) {
        // L'utilisateur n'existe plus en BDD (compte supprimé)
        addCorsHeaders(request, response);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter()
            .write("{\"error\": \"User no longer exists\", \"message\": \"Votre compte a été supprimé\"}");
        return;
      } catch (Exception e) {
        // Token invalide ou expiré
        addCorsHeaders(request, response);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Invalid token\", \"message\": \"Token invalide ou expiré\"}");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private void addCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
    String origin = request.getHeader("Origin");
    if (origin != null) {
      response.setHeader("Access-Control-Allow-Origin", origin);
      response.setHeader("Access-Control-Allow-Credentials", "true");
      response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
      response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
    }
  }

  private void refreshLastLoginAt(UserDetails userDetails) {
    if (!(userDetails instanceof User user)) {
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastLoginAt = user.getLastLoginAt();
    if (lastLoginAt == null || lastLoginAt.isBefore(now.minusHours(LAST_LOGIN_REFRESH_HOURS))) {
      user.setLastLoginAt(now);
      userRepository.save(user);
    }
  }
}

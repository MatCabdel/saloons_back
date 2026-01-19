package com.backend_project_template.security;

import com.backend_project_template.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomUserDetailsService customUserDetailsService;
  private final CustomAuthEntryPoint entryPoint;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      CustomUserDetailsService customUserDetailsService,
      CustomAuthEntryPoint entryPoint) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.customUserDetailsService = customUserDetailsService;
    this.entryPoint = entryPoint;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**")
            .permitAll()
            .requestMatchers("/auth/**")
            .permitAll()
            .requestMatchers("/user/upload/**")
            .permitAll()
            .requestMatchers("/websocket/**")
            .permitAll()
            .requestMatchers("/ws/**")
            .permitAll()
            .requestMatchers("/app/**")
            .permitAll()
            .requestMatchers("/topic/**")
            .permitAll()
            .requestMatchers("/queue/**")
            .permitAll()
            .requestMatchers("/images/**")
            .permitAll()
            .requestMatchers("/swagger-ui.html")
            .permitAll()
            .requestMatchers("/swagger-ui/**")
            .permitAll()
            .requestMatchers("/v3/api-docs/**")
            .permitAll()
            .requestMatchers("/admin/**")
            .hasRole("ADMIN")
            .requestMatchers("/user/**")
            .hasAnyRole("USER", "ADMIN")
            .anyRequest()
            .authenticated())
        .userDetailsService(customUserDetailsService)
        .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
      throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

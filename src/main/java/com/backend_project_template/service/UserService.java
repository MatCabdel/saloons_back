package com.backend_project_template.service;

import com.backend_project_template.Entity.User;
import com.backend_project_template.repository.UserRepository;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User registerUser(String email, String password, Set<String> roles) {
    if (userRepository.existsByEmail(email)) {
      throw new RuntimeException("Cet email est déjà utilisé");
    }
    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.setRoles(roles);
    User savedUser = userRepository.save(user);
    System.out.println("✅ Utilisateur enregistré avec succès : " + savedUser);
    return savedUser;
  }

  public User findById(Long id) {
    return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
  }
}

package com.backend_project_template.service;

import com.backend_project_template.Entity.User;
import com.backend_project_template.dto.UserRegistrationDTO;
import com.backend_project_template.repository.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User registerUser(UserRegistrationDTO dto, Set<String> roles) {
    if (userRepository.existsByEmail(dto.getEmail())) {
      throw new RuntimeException("Cet email est déjà utilisé");
    }
    User user = new User();
    user.setEmail(dto.getEmail());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setUserName(dto.getUsername());
    user.setRoles(roles);
    User savedUser = userRepository.save(user);
    System.out.println("✅ Utilisateur enregistré avec succès : " + savedUser);
    return savedUser;
  }

  public User registerUserWithImage(UserRegistrationDTO dto, Set<String> roles) {
    if (userRepository.existsByEmail(dto.getEmail())) {
      throw new RuntimeException("Cet email est déjà utilisé");
    }
    User user = new User();
    user.setEmail(dto.getEmail());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setUserName(dto.getUsername());
    user.setRoles(roles);

    MultipartFile image = dto.getImage();
    if (image != null && !image.isEmpty()) {
      try {
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path uploadDir = Paths.get("uploads/images/");
        Files.createDirectories(uploadDir);
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        user.setImgUrl(baseUrl + "/user/upload/" + fileName);
      } catch (Exception e) {
        throw new RuntimeException("Erreur lors de l'upload de l'image", e);
      }
    }

    User savedUser = userRepository.save(user);
    return savedUser;
  }

  public User findById(Long id) {
    return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
  }

  public int calculateAge(LocalDate birthDate) {
    if (birthDate == null) {
      return 0;
    }
    return Period.between(birthDate, LocalDate.now()).getYears();
  }
}

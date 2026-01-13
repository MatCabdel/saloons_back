package com.backend_project_template.domains.user;

import com.backend_project_template.domains.auth.dto.UserRegistrationDTO;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.saloonSession.SaloonSession;
import com.backend_project_template.domains.saloonSession.SaloonSessionRepository;
import jakarta.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private SaloonRepository saloonRepository;

  @Autowired
  private SaloonSessionRepository saloonSessionRepository;

  @Autowired
  private UserMapper userMapper;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

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
    user.setDescription(dto.getDescription());
    user.setCity(dto.getCity());
    user.setBirthDate(dto.getBirthDate());

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

  public String saveUserImage(User user, MultipartFile image) {
    try {
      String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
      Path uploadDir = Paths.get("uploads/images/");
      Files.createDirectories(uploadDir);
      Path filePath = uploadDir.resolve(fileName);
      Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
      return baseUrl + "/user/upload/" + fileName;
    } catch (Exception e) {
      throw new RuntimeException("Erreur lors de l'upload de l'image", e);
    }
  }

  @Transactional
  public UserDTO connectUserToSaloon(Long userId, Long saloonId) {
    User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    Saloon saloon = saloonRepository.findById(saloonId).orElseThrow(() -> new RuntimeException("Saloon non trouvé"));

    SaloonSession session = new SaloonSession();
    session.setUser(user);
    session.setSaloon(saloon);
    session.setConnectedAt(LocalDateTime.now());
    saloonSessionRepository.save(session);

    user.setCurrentSaloon(saloon);
    userRepository.save(user);

    return userMapper.toUserDTO(user);
  }

  public UserDTO updateUserProfile(User user, UserProfileUpdateRequest request) {
    if (request == null) {
      UserDTO dto = userMapper.toUserDTO(user);
      dto.setAge(calculateAge(user.getBirthDate()));
      return dto;
    }

    if (request.getUserName() != null) {
      String trimmedUserName = request.getUserName().trim();
      if (trimmedUserName.isEmpty()) {
        throw new IllegalArgumentException("Le pseudo ne peut pas être vide");
      }
      user.setUserName(trimmedUserName);
    }

    if (request.getCity() != null) {
      String trimmedCity = request.getCity().trim();
      user.setCity(trimmedCity.isEmpty() ? null : trimmedCity);
    }

    if (request.getDescription() != null) {
      String trimmedDescription = request.getDescription().trim();
      user.setDescription(trimmedDescription.isEmpty() ? null : trimmedDescription);
    }

    User savedUser = userRepository.save(user);
    UserDTO dto = userMapper.toUserDTO(savedUser);
    dto.setAge(calculateAge(savedUser.getBirthDate()));
    return dto;
  }
}

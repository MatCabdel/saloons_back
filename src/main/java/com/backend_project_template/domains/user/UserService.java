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
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
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
    return userRepository.save(user);
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
      user.setCity(trimmedCity.isEmpty() ? null : normalizeCity(trimmedCity));
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

  /**
   * Find user by email, returning Optional.
   */
  public Optional<User> findByEmailOptional(String email) {
    return userRepository.findByEmail(email);
  }

  /**
   * Check if email already exists.
   */
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  /**
   * Save user.
   */
  public User save(User user) {
    return userRepository.save(user);
  }

  /**
   * Create a new user from Firebase authentication (Google/Facebook/Apple).
   */
  public User createFirebaseUser(
      String email,
      String firebaseUid,
      String displayName,
      String photoUrl,
      AuthProvider authProvider) {
    User user = new User();
    user.setEmail(email);
    user.setFirebaseUid(firebaseUid);
    user.setAuthProvider(authProvider);
    user.setProfileStatus(ProfileStatus.PROFILE_INCOMPLETE);
    user.setRoles(new HashSet<>(Set.of("ROLE_USER")));

    // Try to extract first/last name from display name
    if (displayName != null && !displayName.isEmpty()) {
      String[] parts = displayName.split(" ", 2);
      user.setFirstName(parts[0]);
      if (parts.length > 1) {
        user.setLastName(parts[1]);
      }
    }

    // Set photo URL if available
    if (photoUrl != null && !photoUrl.isEmpty()) {
      user.setImgUrl(photoUrl);
    }

    return userRepository.save(user);
  }

  /**
   * Create a new user with email and password.
   */
  public User createEmailUser(String email, String password, String firstName, String lastName) {
    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setAuthProvider(AuthProvider.EMAIL);
    user.setProfileStatus(ProfileStatus.PROFILE_INCOMPLETE);
    user.setRoles(new HashSet<>(Set.of("ROLE_USER")));
    return userRepository.save(user);
  }

  /**
   * Complete user profile during onboarding.
   */
  @Transactional
  public User completeProfile(User user, CompleteProfileRequest request) {
    user.setUserName(request.getUserName());
    user.setBirthDate(request.getBirthDate());

    if (request.getDescription() != null) {
      user.setDescription(request.getDescription());
    }

    if (request.getCity() != null) {
      String trimmedCity = request.getCity().trim();
      user.setCity(trimmedCity.isEmpty() ? null : normalizeCity(trimmedCity));
    }

    if (request.getPostalCode() != null) {
      String trimmedPostalCode = request.getPostalCode().trim();
      user.setPostalCode(trimmedPostalCode.isEmpty() ? null : trimmedPostalCode);
    }

    // Mark profile as complete
    user.setProfileStatus(ProfileStatus.ACTIVE);

    return userRepository.save(user);
  }

  private String normalizeCity(String city) {
    String normalized = city.trim().replaceAll("\\s+", " ").toLowerCase(Locale.FRANCE);
    StringBuilder builder = new StringBuilder(normalized.length());
    boolean capitalizeNext = true;

    for (char currentChar : normalized.toCharArray()) {
      if (capitalizeNext && Character.isLetter(currentChar)) {
        builder.append(Character.toTitleCase(currentChar));
        capitalizeNext = false;
        continue;
      }

      builder.append(currentChar);
      capitalizeNext = Character.isWhitespace(currentChar) || currentChar == '-' || currentChar == '\'';
    }

    return builder.toString();
  }
}

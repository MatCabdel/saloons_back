package com.backend_project_template.domains.admin;

import com.backend_project_template.domains.auth.FirebaseAuthService;
import com.backend_project_template.domains.conversation.ConversationParticipantRepository;
import com.backend_project_template.domains.conversation.ConversationRepository;
import com.backend_project_template.domains.heartRequest.HeartRequestRepository;
import com.backend_project_template.domains.match.MatchRepository;
import com.backend_project_template.domains.match.UserLikeRepository;
import com.backend_project_template.domains.message.MessageRepository;
import com.backend_project_template.domains.report.ReportRepository;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonDTO;
import com.backend_project_template.domains.saloon.SaloonMapper;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.saloon.SaloonType;
import com.backend_project_template.domains.saloonChat.SaloonMessageRepository;
import com.backend_project_template.domains.saloonDemande.SaloonDemandeRepository;
import com.backend_project_template.domains.saloonSession.SaloonSessionRepository;
import com.backend_project_template.domains.session.SessionRedisService;
import com.backend_project_template.domains.subscription.PremiumSubscriptionRepository;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserDTO;
import com.backend_project_template.domains.user.UserRepository;
import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
@SuppressWarnings("checkstyle:ParameterNumber")
public class AdminController {

  private static final String UPLOAD_DIR = "uploads/images/";
  private static final int WEEKS_FOR_ACTIVE_USER = 3;
  private static final int MONTHS_TO_FETCH = 11;
  private static final int HOUR_END_OF_DAY = 23;
  private static final int MINUTE_END_OF_DAY = 59;
  private static final int SECOND_END_OF_DAY = 59;
  private static final int DEFAULT_RADIUS_METERS = 100;

  private final UserRepository userRepository;
  private final SaloonRepository saloonRepository;
  private final SaloonMapper saloonMapper;
  private final SessionRedisService sessionRedisService;
  private final PremiumSubscriptionRepository premiumSubscriptionRepository;
  private final UserLikeRepository userLikeRepository;
  private final MatchRepository matchRepository;
  private final MessageRepository messageRepository;
  private final SaloonMessageRepository saloonMessageRepository;
  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository conversationParticipantRepository;
  private final SaloonSessionRepository saloonSessionRepository;
  private final FirebaseAuthService firebaseAuthService;
  private final HeartRequestRepository heartRequestRepository;
  private final ReportRepository reportRepository;
  private final SaloonDemandeRepository saloonDemandeRepository;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  public AdminController(
      UserRepository userRepository,
      SaloonRepository saloonRepository,
      SaloonMapper saloonMapper,
      SessionRedisService sessionRedisService,
      PremiumSubscriptionRepository premiumSubscriptionRepository,
      UserLikeRepository userLikeRepository,
      MatchRepository matchRepository,
      MessageRepository messageRepository,
      SaloonMessageRepository saloonMessageRepository,
      ConversationRepository conversationRepository,
      ConversationParticipantRepository conversationParticipantRepository,
      SaloonSessionRepository saloonSessionRepository,
      FirebaseAuthService firebaseAuthService,
      HeartRequestRepository heartRequestRepository,
      ReportRepository reportRepository,
      SaloonDemandeRepository saloonDemandeRepository) {
    this.userRepository = userRepository;
    this.saloonRepository = saloonRepository;
    this.saloonMapper = saloonMapper;
    this.sessionRedisService = sessionRedisService;
    this.premiumSubscriptionRepository = premiumSubscriptionRepository;
    this.userLikeRepository = userLikeRepository;
    this.matchRepository = matchRepository;
    this.messageRepository = messageRepository;
    this.saloonMessageRepository = saloonMessageRepository;
    this.conversationRepository = conversationRepository;
    this.conversationParticipantRepository = conversationParticipantRepository;
    this.saloonSessionRepository = saloonSessionRepository;
    this.firebaseAuthService = firebaseAuthService;
    this.heartRequestRepository = heartRequestRepository;
    this.reportRepository = reportRepository;
    this.saloonDemandeRepository = saloonDemandeRepository;
  }

  @GetMapping("/statistics")
  public ResponseEntity<DashboardStatsDTO> getStatistics() {
    long totalUsers = userRepository.count();
    LocalDateTime threeWeeksAgo = LocalDateTime.now().minusWeeks(WEEKS_FOR_ACTIVE_USER);
    long activeUsers = userRepository.countByLastLoginAtAfter(threeWeeksAgo);
    long premiumUsers = userRepository.countByIsPremiumTrue();
    long totalSaloons = saloonRepository.count();
    int connectedUsers = sessionRedisService.getTotalConnectedUsers();

    List<Saloon> allSaloons = saloonRepository.findAll();
    Map<String, Long> saloonsByCity = allSaloons.stream()
        .filter(s -> s.getCity() != null && !s.getCity().isEmpty())
        .collect(Collectors.groupingBy(Saloon::getCity, Collectors.counting()));

    DashboardStatsDTO stats = new DashboardStatsDTO(
        totalUsers,
        activeUsers,
        premiumUsers,
        totalSaloons,
        connectedUsers,
        saloonsByCity);

    return ResponseEntity.ok(stats);
  }

  @GetMapping("/statistics/premium-evolution")
  public ResponseEntity<List<PremiumMonthlyStatsDTO>> getPremiumEvolution() {
    List<PremiumMonthlyStatsDTO> evolution = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();

    // Récupérer les 12 derniers mois
    for (int i = MONTHS_TO_FETCH; i >= 0; i--) {
      LocalDateTime monthDate = now.minusMonths(i);
      int year = monthDate.getYear();
      int month = monthDate.getMonthValue();

      // Calculer le nombre d'abonnements actifs à la fin de ce mois
      LocalDateTime endOfMonth = monthDate.withDayOfMonth(monthDate.toLocalDate().lengthOfMonth())
          .withHour(HOUR_END_OF_DAY).withMinute(MINUTE_END_OF_DAY).withSecond(SECOND_END_OF_DAY);
      long activeSubscriptions = premiumSubscriptionRepository.countActiveAtDate(endOfMonth);

      String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.FRENCH);
      evolution.add(new PremiumMonthlyStatsDTO(year, month, monthName, activeSubscriptions));
    }

    return ResponseEntity.ok(evolution);
  }

  @GetMapping("/statistics/by-city")
  public ResponseEntity<CityStatsDTO> getCityStatistics() {
    // Utilisateurs par ville (inscrits)
    List<Object[]> usersByCity = userRepository.countByCity();
    Map<String, Long> usersByCityMap = usersByCity.stream()
        .collect(Collectors.toMap(
            row -> (String) row[0],
            row -> (Long) row[1]));

    // Utilisateurs connectés dans des saloons par ville
    List<Object[]> connectedByCity = userRepository.countConnectedByCity();
    Map<String, Long> connectedByCityMap = connectedByCity.stream()
        .collect(Collectors.toMap(
            row -> (String) row[0],
            row -> (Long) row[1]));

    return ResponseEntity.ok(new CityStatsDTO(usersByCityMap, connectedByCityMap));
  }

  @GetMapping("/statistics/saloons-by-city")
  public ResponseEntity<SaloonsByCityStatsDTO> getSaloonsStatisticsByCity() {
    List<Saloon> allSaloons = saloonRepository.findAll();

    // Compter les saloons par ville
    Map<String, Long> saloonCountByCity = allSaloons.stream()
        .filter(s -> s.getCity() != null && !s.getCity().isEmpty())
        .collect(Collectors.groupingBy(Saloon::getCity, Collectors.counting()));

    // Grouper les saloons par ville avec leur nombre de connectés
    Map<String, List<SaloonStatsItemDTO>> saloonsByCity = new HashMap<>();
    for (Saloon saloon : allSaloons) {
      if (saloon.getCity() != null && !saloon.getCity().isEmpty()) {
        int connectedCount = sessionRedisService.getPresenceCount(saloon.getId());
        SaloonStatsItemDTO item = new SaloonStatsItemDTO(
            saloon.getId(),
            saloon.getName(),
            saloon.getCity(),
            saloon.getImgUrl(),
            connectedCount);
        saloonsByCity.computeIfAbsent(saloon.getCity(), k -> new ArrayList<>()).add(item);
      }
    }

    return ResponseEntity.ok(new SaloonsByCityStatsDTO(saloonCountByCity, saloonsByCity));
  }

  /**
   * Get all saloons with pagination and sorting for admin dashboard
   * Supports sorting by: name (asc/desc), connectedCount (handled client-side
   * since it's from Redis)
   * Supports searching by: name
   */
  @GetMapping("/saloons")
  public ResponseEntity<?> getAllSaloonsAdmin(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "name") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir,
      @RequestParam(required = false) String search) {
    // Si page=-1, retourner tous les saloons sans pagination (compatibilité)
    if (page < 0) {
      List<Saloon> saloons = saloonRepository.findAll();
      if (saloons.isEmpty()) {
        return ResponseEntity.noContent().build();
      }
      List<SaloonDTO> dtos = saloons.stream()
          .map(saloon -> {
            SaloonDTO dto = saloonMapper.toSaloonDTO(saloon);
            dto.setConnectedCount(sessionRedisService.getPresenceCount(saloon.getId()));
            return dto;
          })
          .toList();
      return ResponseEntity.ok(dtos);
    }

    // Pagination avec tri
    Sort sort = sortDir.equalsIgnoreCase("desc")
        ? Sort.by(sortBy).descending()
        : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<Saloon> saloonPage;
    if (search != null && !search.trim().isEmpty()) {
      saloonPage = saloonRepository.searchSaloons(search.trim(), pageable);
    } else {
      saloonPage = saloonRepository.findAll(pageable);
    }

    List<SaloonDTO> dtos = saloonPage.getContent().stream()
        .map(saloon -> {
          SaloonDTO dto = saloonMapper.toSaloonDTO(saloon);
          dto.setConnectedCount(sessionRedisService.getPresenceCount(saloon.getId()));
          return dto;
        })
        .toList();

    // Pour le tri par connectedCount, on doit le faire côté serveur après avoir
    // enrichi les DTOs
    if ("connectedCount".equals(sortBy)) {
      Comparator<SaloonDTO> comparator = Comparator.comparingInt(SaloonDTO::getConnectedCount);
      if ("desc".equalsIgnoreCase(sortDir)) {
        comparator = comparator.reversed();
      }
      dtos = dtos.stream().sorted(comparator).toList();
    }

    return ResponseEntity.ok(PagedResponseDTO.of(
        dtos,
        page,
        size,
        saloonPage.getTotalElements()));
  }

  /**
   * Get all users with pagination and sorting for admin dashboard
   * Supports sorting by: userName (asc/desc), lastLoginAt (asc/desc)
   * Supports searching by: userName, firstname, lastname, email
   */
  @GetMapping("/users")
  public ResponseEntity<PagedResponseDTO<UserDTO>> getAllUsersAdmin(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "userName") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir,
      @RequestParam(required = false) String search) {
    Sort sort = sortDir.equalsIgnoreCase("desc")
        ? Sort.by(sortBy).descending()
        : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<User> userPage;
    if (search != null && !search.trim().isEmpty()) {
      userPage = userRepository.searchUsers(search.trim(), pageable);
    } else {
      userPage = userRepository.findAll(pageable);
    }

    List<UserDTO> dtos = userPage.getContent().stream()
        .map(user -> {
          UserDTO dto = new UserDTO(user);
          if (user.getBirthDate() != null) {
            int age = java.time.Period.between(user.getBirthDate(), java.time.LocalDate.now()).getYears();
            dto.setAge(age);
          }
          return dto;
        })
        .toList();

    return ResponseEntity.ok(PagedResponseDTO.of(
        dtos,
        page,
        size,
        userPage.getTotalElements()));
  }

  @GetMapping("/saloon/{id}/users")
  public ResponseEntity<List<UserDTO>> getSaloonUsers(@PathVariable Long id) {
    // Récupérer les utilisateurs actuellement connectés depuis Redis
    java.util.Set<String> connectedUserIds = sessionRedisService.getPresenceUserIds(id);

    if (connectedUserIds == null || connectedUserIds.isEmpty()) {
      return ResponseEntity.ok(List.of());
    }

    List<UserDTO> dtos = connectedUserIds.stream()
        .map(userIdStr -> {
          Long userId = Long.parseLong(userIdStr);
          return userRepository.findById(userId).orElse(null);
        })
        .filter(user -> user != null)
        .map(user -> {
          UserDTO dto = new UserDTO(user);
          dto.setAge(user.getBirthDate() != null
              ? java.time.Period.between(user.getBirthDate(), java.time.LocalDate.now()).getYears()
              : 0);
          return dto;
        })
        .toList();

    return ResponseEntity.ok(dtos);
  }

  @PostMapping("/saloon")
  public ResponseEntity<SaloonDTO> createSaloon(@Valid @RequestBody CreateSaloonRequest request) {
    Saloon saloon = new Saloon();
    saloon.setName(request.getName());
    saloon.setImgUrl(request.getImgUrl());
    saloon.setAddress(request.getAddress());
    saloon.setCity(request.getCity());
    saloon.setCountry(request.getCountry());
    saloon.setLatitude(request.getLatitude());
    saloon.setLongitude(request.getLongitude());
    saloon.setRadiusMeters(
        request.getRadiusMeters() != null ? request.getRadiusMeters() : DEFAULT_RADIUS_METERS);
    saloon.setType(request.getType() != null ? request.getType() : SaloonType.BAR);
    saloon.setVisitorNumber(0);
    saloon.setCreatedAt(LocalDateTime.now());
    saloon.setIsActive(true);
    if (request.getIsPrivate() != null) {
      saloon.setIsPrivate(request.getIsPrivate());
    }

    Saloon savedSaloon = saloonRepository.save(saloon);
    return ResponseEntity.ok(saloonMapper.toSaloonDTO(savedSaloon));
  }

  @SuppressWarnings("checkstyle:ParameterNumber")
  @PostMapping("/saloon/upload")
  public ResponseEntity<SaloonDTO> createSaloonWithImage(
      @RequestParam("file") MultipartFile file,
      @RequestParam("name") String name,
      @RequestParam(value = "address", required = false) String address,
      @RequestParam(value = "city", required = false) String city,
      @RequestParam(value = "country", required = false, defaultValue = "France") String country,
      @RequestParam("latitude") BigDecimal latitude,
      @RequestParam("longitude") BigDecimal longitude,
      @RequestParam(value = "radiusMeters", required = false, defaultValue = "100") Integer radiusMeters,
      @RequestParam(value = "type", required = false, defaultValue = "BAR") SaloonType type,
      @RequestParam(value = "isPrivate", required = false) Boolean isPrivate) {
    try {
      String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
      Path filePath = Paths.get(UPLOAD_DIR + fileName);

      Files.createDirectories(filePath.getParent());
      Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

      String imageUrl = baseUrl + "/user/upload/" + fileName;

      Saloon saloon = new Saloon();
      saloon.setName(name);
      saloon.setImgUrl(imageUrl);
      saloon.setAddress(address);
      saloon.setCity(city);
      saloon.setCountry(country);
      saloon.setLatitude(latitude);
      saloon.setLongitude(longitude);
      saloon.setRadiusMeters(radiusMeters);
      saloon.setType(type);
      saloon.setVisitorNumber(0);
      saloon.setCreatedAt(LocalDateTime.now());
      saloon.setIsActive(true);
      if (isPrivate != null) {
        saloon.setIsPrivate(isPrivate);
      }

      Saloon savedSaloon = saloonRepository.save(saloon);
      return ResponseEntity.ok(saloonMapper.toSaloonDTO(savedSaloon));
    } catch (IOException e) {
      return ResponseEntity.<SaloonDTO>status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/saloon/{id}")
  public ResponseEntity<SaloonDTO> getSaloonById(@PathVariable Long id) {
    return saloonRepository.findById(id)
        .map(saloon -> {
          SaloonDTO dto = saloonMapper.toSaloonDTO(saloon);
          dto.setConnectedCount(sessionRedisService.getPresenceCount(saloon.getId()));
          return ResponseEntity.ok(dto);
        })
        .orElse(ResponseEntity.<SaloonDTO>notFound().build());
  }

  @PutMapping("/saloon/{id}")
  public ResponseEntity<SaloonDTO> updateSaloon(
      @PathVariable Long id,
      @Valid @RequestBody CreateSaloonRequest request) {
    return saloonRepository.findById(id)
        .map(saloon -> {
          saloon.setName(request.getName());
          saloon.setImgUrl(request.getImgUrl());
          saloon.setAddress(request.getAddress());
          saloon.setCity(request.getCity());
          saloon.setCountry(request.getCountry());
          saloon.setLatitude(request.getLatitude());
          saloon.setLongitude(request.getLongitude());
          if (request.getRadiusMeters() != null) {
            saloon.setRadiusMeters(request.getRadiusMeters());
          }
          if (request.getType() != null) {
            saloon.setType(request.getType());
          }
          if (request.getIsPrivate() != null) {
            saloon.setIsPrivate(request.getIsPrivate());
          }
          Saloon savedSaloon = saloonRepository.save(saloon);
          return ResponseEntity.ok(saloonMapper.toSaloonDTO(savedSaloon));
        })
        .orElse(ResponseEntity.<SaloonDTO>notFound().build());
  }

  @SuppressWarnings("checkstyle:ParameterNumber")
  @PutMapping("/saloon/{id}/upload")
  public ResponseEntity<SaloonDTO> updateSaloonWithImage(
      @PathVariable Long id,
      @RequestParam("file") MultipartFile file,
      @RequestParam("name") String name,
      @RequestParam(value = "address", required = false) String address,
      @RequestParam(value = "city", required = false) String city,
      @RequestParam(value = "country", required = false) String country,
      @RequestParam("latitude") BigDecimal latitude,
      @RequestParam("longitude") BigDecimal longitude,
      @RequestParam(value = "radiusMeters", required = false) Integer radiusMeters,
      @RequestParam(value = "type", required = false) SaloonType type,
      @RequestParam(value = "isPrivate", required = false) Boolean isPrivate) {
    Saloon saloon = saloonRepository.findById(id).orElse(null);
    if (saloon == null) {
      return ResponseEntity.<SaloonDTO>notFound().build();
    }

    try {
      String originalFilename = file.getOriginalFilename();
      String extension = originalFilename != null
          ? originalFilename.substring(originalFilename.lastIndexOf("."))
          : ".jpg";
      String filename = UUID.randomUUID().toString() + extension;
      Path uploadPath = Paths.get(UPLOAD_DIR);
      Files.createDirectories(uploadPath);
      Path filePath = uploadPath.resolve(filename);
      Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
      String imageUrl = baseUrl + "/uploads/images/" + filename;

      saloon.setName(name);
      saloon.setImgUrl(imageUrl);
      saloon.setAddress(address);
      saloon.setCity(city);
      saloon.setCountry(country);
      saloon.setLatitude(latitude);
      saloon.setLongitude(longitude);
      if (radiusMeters != null) {
        saloon.setRadiusMeters(radiusMeters);
      }
      if (type != null) {
        saloon.setType(type);
      }
      if (isPrivate != null) {
        saloon.setIsPrivate(isPrivate);
      }
      Saloon savedSaloon = saloonRepository.save(saloon);
      return ResponseEntity.ok(saloonMapper.toSaloonDTO(savedSaloon));
    } catch (IOException e) {
      return ResponseEntity.<SaloonDTO>status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PatchMapping("/saloon/{id}/toggle-active")
  public ResponseEntity<SaloonDTO> toggleSaloonActive(@PathVariable Long id) {
    return saloonRepository.findById(id)
        .map(saloon -> {
          saloon.setIsActive(!saloon.getIsActive());
          Saloon savedSaloon = saloonRepository.save(saloon);
          return ResponseEntity.ok(saloonMapper.toSaloonDTO(savedSaloon));
        })
        .orElse(ResponseEntity.<SaloonDTO>notFound().build());
  }

  @PatchMapping("/saloon/{id}/toggle-private")
  public ResponseEntity<SaloonDTO> toggleSaloonPrivate(@PathVariable Long id) {
    return saloonRepository.findById(id)
        .map(saloon -> {
          saloon.setIsPrivate(!saloon.getIsPrivate());
          Saloon savedSaloon = saloonRepository.save(saloon);
          return ResponseEntity.ok(saloonMapper.toSaloonDTO(savedSaloon));
        })
        .orElse(ResponseEntity.<SaloonDTO>notFound().build());
  }

  @DeleteMapping("/saloon/{id}")
  public ResponseEntity<Void> deleteSaloon(@PathVariable Long id) {
    if (!saloonRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    saloonRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/user/{id}/toggle-premium")
  public ResponseEntity<Map<String, Object>> toggleUserPremium(@PathVariable Long id) {
    User user = userRepository.findById(id).orElse(null);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }

    Boolean currentPremium = user.getIsPremium();
    boolean newPremium = currentPremium == null || !currentPremium;
    user.setIsPremium(newPremium);

    if (newPremium) {
      user.setPremiumStartDate(LocalDateTime.now());
      user.setPremiumEndDate(null); // Illimité quand défini par admin
    } else {
      user.setPremiumStartDate(null);
      user.setPremiumEndDate(null);
    }

    userRepository.save(user);

    Map<String, Object> response = new HashMap<>();
    response.put("id", user.getId());
    response.put("isPremium", newPremium);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/user/{id}")
  @jakarta.transaction.Transactional
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    User user = userRepository.findById(id).orElse(null);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }

    // Supprimer l'utilisateur de Firebase Auth (pour éviter qu'il puisse se
    // reconnecter)
    if (user.getFirebaseUid() != null) {
      firebaseAuthService.deleteUser(user.getFirebaseUid());
    }

    // Nettoyer Redis : session + présence dans tous les saloons
    sessionRedisService.deleteSession(id);
    sessionRedisService.removeUserFromAllPresence(id);

    // Supprimer les sessions de saloon (historique)
    saloonSessionRepository.deleteByUser(user);

    // Détacher l'utilisateur du saloon actuel
    user.setCurrentSaloon(null);
    userRepository.save(user);

    // Supprimer les likes (où l'utilisateur est liker ou liked)
    userLikeRepository.deleteByLiker(user);
    userLikeRepository.deleteByLiked(user);

    // Supprimer les matchs (où l'utilisateur est user1 ou user2)
    matchRepository.deleteByUser1(user);
    matchRepository.deleteByUser2(user);

    // Supprimer les demandes de coup de cœur (envoyées et reçues)
    heartRequestRepository.deleteAll(heartRequestRepository.findBySenderId(id));
    heartRequestRepository.deleteAll(heartRequestRepository.findByReceiverId(id));

    // Supprimer les messages privés envoyés par l'utilisateur
    messageRepository.deleteBySender(user);

    // Supprimer les messages de chat de saloon envoyés par l'utilisateur
    saloonMessageRepository.deleteBySender(user);

    // Supprimer les abonnements premium
    premiumSubscriptionRepository.deleteByUser(user);

    // Supprimer les participations aux conversations (table de jointure)
    conversationParticipantRepository.deleteByUserId(id);

    // Supprimer les signalements (faits par ou contre l'utilisateur)
    reportRepository.deleteByReporter(user);
    reportRepository.deleteByReported(user);

    // Supprimer les demandes de saloon
    saloonDemandeRepository.deleteByUser(user);

    // Enfin, supprimer l'utilisateur
    userRepository.delete(user);

    return ResponseEntity.noContent().build();
  }

  /**
   * Nettoie les utilisateurs Firebase orphelins (présents dans Firebase mais pas
   * dans la BDD).
   * DELETE /admin/cleanup/firebase-orphans
   */
  @DeleteMapping("/cleanup/firebase-orphans")
  public ResponseEntity<Map<String, Object>> cleanupFirebaseOrphans() {
    // Récupérer tous les UIDs Firebase
    java.util.List<String> firebaseUids = firebaseAuthService.getAllFirebaseUserUids();

    // Récupérer tous les firebaseUids de la BDD
    java.util.Set<String> dbFirebaseUids = userRepository.findAll().stream()
        .map(User::getFirebaseUid)
        .filter(uid -> uid != null)
        .collect(java.util.stream.Collectors.toSet());

    // Trouver les orphelins (dans Firebase mais pas dans BDD)
    java.util.List<String> orphans = firebaseUids.stream()
        .filter(uid -> !dbFirebaseUids.contains(uid))
        .collect(java.util.stream.Collectors.toList());

    // Supprimer les orphelins
    int deletedCount = 0;
    java.util.List<String> failedDeletions = new java.util.ArrayList<>();

    for (String orphanUid : orphans) {
      if (firebaseAuthService.deleteUser(orphanUid)) {
        deletedCount++;
      } else {
        failedDeletions.add(orphanUid);
      }
    }

    Map<String, Object> result = new HashMap<>();
    result.put("firebaseUsersCount", firebaseUids.size());
    result.put("dbUsersCount", dbFirebaseUids.size());
    result.put("orphansFound", orphans.size());
    result.put("deletedCount", deletedCount);
    result.put("failedDeletions", failedDeletions);

    return ResponseEntity.ok(result);
  }
}

package com.backend_project_template.domains.admin;

import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonDTO;
import com.backend_project_template.domains.saloon.SaloonMapper;
import com.backend_project_template.domains.saloon.SaloonRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
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

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  public AdminController(
      UserRepository userRepository,
      SaloonRepository saloonRepository,
      SaloonMapper saloonMapper,
      SessionRedisService sessionRedisService,
      PremiumSubscriptionRepository premiumSubscriptionRepository) {
    this.userRepository = userRepository;
    this.saloonRepository = saloonRepository;
    this.saloonMapper = saloonMapper;
    this.sessionRedisService = sessionRedisService;
    this.premiumSubscriptionRepository = premiumSubscriptionRepository;
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

  @GetMapping("/saloon/{id}/users")
  public ResponseEntity<List<UserDTO>> getSaloonUsers(@PathVariable Long id) {
    return saloonRepository.findById(id)
        .map(saloon -> {
          List<User> users = saloon.getUsersInSaloon();
          if (users == null || users.isEmpty()) {
            return ResponseEntity.ok(List.<UserDTO>of());
          }
          List<UserDTO> dtos = users.stream()
              .map(user -> {
                UserDTO dto = new UserDTO(user);
                dto.setAge(user.getBirthDate() != null
                    ? java.time.Period.between(user.getBirthDate(), java.time.LocalDate.now()).getYears()
                    : 0);
                return dto;
              })
              .toList();
          return ResponseEntity.ok(dtos);
        })
        .orElse(ResponseEntity.notFound().build());
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
    saloon.setVisitorNumber(0);
    saloon.setCreatedAt(LocalDateTime.now());
    saloon.setIsActive(true);

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
      @RequestParam(value = "radiusMeters", required = false, defaultValue = "100") Integer radiusMeters) {
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
      saloon.setVisitorNumber(0);
      saloon.setCreatedAt(LocalDateTime.now());
      saloon.setIsActive(true);

      Saloon savedSaloon = saloonRepository.save(saloon);
      return ResponseEntity.ok(saloonMapper.toSaloonDTO(savedSaloon));
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
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
          Saloon savedSaloon = saloonRepository.save(saloon);
          return ResponseEntity.ok(saloonMapper.toSaloonDTO(savedSaloon));
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @PatchMapping("/saloon/{id}/toggle-active")
  public ResponseEntity<SaloonDTO> toggleSaloonActive(@PathVariable Long id) {
    return saloonRepository.findById(id)
        .map(saloon -> {
          saloon.setIsActive(!saloon.getIsActive());
          Saloon savedSaloon = saloonRepository.save(saloon);
          return ResponseEntity.ok(saloonMapper.toSaloonDTO(savedSaloon));
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/saloon/{id}")
  public ResponseEntity<Void> deleteSaloon(@PathVariable Long id) {
    if (!saloonRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    saloonRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}

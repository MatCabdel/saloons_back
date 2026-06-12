package com.backend_project_template.domains.saloon;

import com.backend_project_template.domains.presence.SaloonMapDTO;
import com.backend_project_template.domains.session.SessionRedisService;
import com.backend_project_template.domains.user.UserDTO;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import com.backend_project_template.core.Constant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/saloon")
public class SaloonController {

  private final SaloonRepository saloonRepository;
  private final SessionRedisService sessionRedisService;
  private final UserRepository userRepository;

  @Autowired
  private SaloonMapper saloonMapper;

  public SaloonController(SaloonRepository saloonRepository, SessionRedisService sessionRedisService,
      UserRepository userRepository) {
    this.saloonRepository = saloonRepository;
    this.sessionRedisService = sessionRedisService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public ResponseEntity<List<SaloonDTO>> getAllSaloons(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) SaloonType type) {
    User user = getCurrentUser(userDetails);
    List<Saloon> saloons = getFilteredSaloons(user, type);
    Map<Long, Integer> presenceCounts = sessionRedisService.getAllPresenceCounts();
    List<SaloonDTO> dtos = saloons.stream()
        .map(saloon -> {
          SaloonDTO dto = saloonMapper.toSaloonDTO(saloon);
          dto.setConnectedCount(presenceCounts.getOrDefault(saloon.getId(), 0));
          return dto;
        })
        .toList();
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/map")
  @SuppressWarnings("checkstyle:ParameterNumber")
  public ResponseEntity<List<SaloonMapDTO>> getSaloonsForMap(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam java.math.BigDecimal minLat,
      @RequestParam java.math.BigDecimal maxLat,
      @RequestParam java.math.BigDecimal minLng,
      @RequestParam java.math.BigDecimal maxLng,
      @RequestParam(required = false) SaloonType type) {
    User user = getCurrentUser(userDetails);
    BboxRequest bbox = new BboxRequest(minLat, maxLat, minLng, maxLng, type);
    List<Saloon> saloons = getFilteredSaloonsInBbox(user, bbox);
    Map<Long, Integer> presenceCounts = sessionRedisService.getAllPresenceCounts();
    List<SaloonMapDTO> dtos = saloons.stream()
        .map(s -> saloonMapper.toSaloonMapDTO(s, presenceCounts.getOrDefault(s.getId(), 0)))
        .toList();
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<SaloonDTO> getSaloonById(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    User user = getCurrentUser(userDetails);
    Saloon saloon = saloonRepository.findById(id).orElse(null);
    if (saloon == null) {
      return ResponseEntity.<SaloonDTO>notFound().build();
    }
    if (Boolean.TRUE.equals(saloon.getIsPrivate()) && !canAccessPrivateSaloons(user)) {
      return ResponseEntity.<SaloonDTO>status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(saloonMapper.toSaloonDTO(saloon));
  }

  @GetMapping("/{id}/users")
  public ResponseEntity<List<UserDTO>> getUsersInSaloon(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    User user = getCurrentUser(userDetails);
    Saloon saloon = saloonRepository.findById(id).orElse(null);
    if (saloon == null) {
      return ResponseEntity.notFound().build();
    }
    if (Boolean.TRUE.equals(saloon.getIsPrivate()) && !canAccessPrivateSaloons(user)) {
      return ResponseEntity.<List<UserDTO>>status(HttpStatus.FORBIDDEN).build();
    }

    // Récupérer les utilisateurs actuellement connectés depuis Redis
    Set<String> connectedUserIds = sessionRedisService.getPresenceUserIds(id);

    if (connectedUserIds == null || connectedUserIds.isEmpty()) {
      return ResponseEntity.ok(List.of());
    }

    List<UserDTO> dtos = connectedUserIds.stream()
        .map(userIdStr -> {
          Long userId = Long.parseLong(userIdStr);
          return userRepository.findById(userId).orElse(null);
        })
        .filter(foundUser -> foundUser != null)
        .map(foundUser -> {
          UserDTO dto = new UserDTO(foundUser);
          dto.setAge(foundUser.getBirthDate() != null
              ? java.time.Period.between(foundUser.getBirthDate(), java.time.LocalDate.now()).getYears()
              : 0);
          return dto;
        })
        .toList();

    return ResponseEntity.ok(dtos);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSaloon(@PathVariable Long id) {
    if (!saloonRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    saloonRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private User getCurrentUser(UserDetails userDetails) {
    if (userDetails == null) {
      throw new RuntimeException("Utilisateur non authentifié");
    }
    return userRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
  }

  private boolean canAccessPrivateSaloons(User user) {
    return user.getRoles().contains(Constant.REVIEWER) || user.getRoles().contains(Constant.ADMIN);
  }

  private List<Saloon> getFilteredSaloons(User user, SaloonType type) {
    boolean canSeePrivate = canAccessPrivateSaloons(user);
    if (type != null) {
      return canSeePrivate
          ? saloonRepository.findByIsActiveTrueAndType(type)
          : saloonRepository.findByIsActiveTrueAndIsPrivateFalseAndType(type);
    }
    return canSeePrivate
        ? saloonRepository.findByIsActiveTrue()
        : saloonRepository.findByIsActiveTrueAndIsPrivateFalse();
  }

  private List<Saloon> getFilteredSaloonsInBbox(User user, BboxRequest bbox) {
    boolean canSeePrivate = canAccessPrivateSaloons(user);
    if (bbox.type() == null) {
      return canSeePrivate
          ? saloonRepository.findByBoundingBox(
              bbox.minLat(), bbox.maxLat(), bbox.minLng(), bbox.maxLng())
          : saloonRepository.findPublicByBoundingBox(
              bbox.minLat(), bbox.maxLat(), bbox.minLng(), bbox.maxLng());
    }
    return canSeePrivate
        ? saloonRepository.findByBoundingBoxAndType(
            bbox.minLat(), bbox.maxLat(), bbox.minLng(), bbox.maxLng(), bbox.type())
        : saloonRepository.findPublicByBoundingBoxAndType(
            bbox.minLat(), bbox.maxLat(), bbox.minLng(), bbox.maxLng(), bbox.type());
  }
}

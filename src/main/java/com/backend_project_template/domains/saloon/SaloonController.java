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
      @RequestParam(required = false) SaloonType type,
      @AuthenticationPrincipal UserDetails userDetails) {
    User user = getCurrentUser(userDetails);
    List<Saloon> saloons = getFilteredSaloons(type, canAccessPrivateSaloons(user));

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
  public ResponseEntity<List<SaloonMapDTO>> getSaloonsForMap(
      @ModelAttribute BboxRequest bbox,
      @RequestParam(required = false) SaloonType type,
      @AuthenticationPrincipal UserDetails userDetails) {
    User user = getCurrentUser(userDetails);
    boolean includePrivate = canAccessPrivateSaloons(user);

    List<Saloon> saloons = getFilteredSaloonsInBbox(bbox, type, includePrivate);

    Map<Long, Integer> presenceCounts = sessionRedisService.getAllPresenceCounts();
    List<SaloonMapDTO> dtos = saloons.stream()
        .map(saloon -> saloonMapper.toSaloonMapDTO(
            saloon, presenceCounts.getOrDefault(saloon.getId(), 0)))
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

  private List<Saloon> getFilteredSaloons(SaloonType type, boolean includePrivate) {
    if (type != null) {
      return includePrivate
          ? saloonRepository.findByIsActiveTrueAndType(type)
          : saloonRepository.findByIsActiveTrueAndIsPrivateFalseAndType(type);
    }

    return includePrivate
        ? saloonRepository.findByIsActiveTrue()
        : saloonRepository.findByIsActiveTrueAndIsPrivateFalse();
  }

  private List<Saloon> getFilteredSaloonsInBbox(
      BboxRequest bbox, SaloonType type, boolean includePrivate) {
    if (type != null) {
      return includePrivate
          ? saloonRepository.findByBoundingBoxAndType(
              bbox.minLat(), bbox.maxLat(), bbox.minLng(), bbox.maxLng(), type)
          : saloonRepository.findPublicByBoundingBoxAndType(
              bbox.minLat(), bbox.maxLat(), bbox.minLng(), bbox.maxLng(), type);
    }
    return includePrivate
        ? saloonRepository.findByBoundingBox(
            bbox.minLat(), bbox.maxLat(), bbox.minLng(), bbox.maxLng())
        : saloonRepository.findPublicByBoundingBox(
            bbox.minLat(), bbox.maxLat(), bbox.minLng(), bbox.maxLng());
  }
}

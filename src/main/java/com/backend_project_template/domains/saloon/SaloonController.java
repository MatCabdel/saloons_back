package com.backend_project_template.domains.saloon;

import com.backend_project_template.domains.session.SessionRedisService;
import com.backend_project_template.domains.user.UserDTO;
import com.backend_project_template.domains.user.UserRepository;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<List<SaloonDTO>> getAllSaloons() {
    // Retourner uniquement les saloons actifs pour les utilisateurs
    List<Saloon> saloons = saloonRepository.findByIsActiveTrue();
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

  @GetMapping("/{id}")
  public ResponseEntity<SaloonDTO> getSaloonById(@PathVariable Long id) {
    return saloonRepository.findById(id).map(saloon -> ResponseEntity.ok(saloonMapper.toSaloonDTO(saloon)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/users")
  public ResponseEntity<List<UserDTO>> getUsersInSaloon(@PathVariable Long id) {
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

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSaloon(@PathVariable Long id) {
    if (!saloonRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    saloonRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}

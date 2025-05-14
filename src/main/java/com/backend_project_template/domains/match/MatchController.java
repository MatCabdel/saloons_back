package com.backend_project_template.domains.match;

import com.backend_project_template.Entity.User;
import com.backend_project_template.repository.UserRepository;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/match")
public class MatchController {

  private final MatchService matchService;
  private final UserRepository userRepository;

  public MatchController(MatchService matchService, UserRepository userRepository) {
    this.matchService = matchService;
    this.userRepository = userRepository;
  }

  @PostMapping("/{userId1}/like/{userId2}")
  public ResponseEntity<?> likeUser(@PathVariable Long userId1, @PathVariable Long userId2) {
    User u1 = userRepository.findById(userId1).orElseThrow();
    User u2 = userRepository.findById(userId2).orElseThrow();
    boolean matched = matchService.like(u1, u2);
    if (matched) {
      return ResponseEntity.ok(Map.of("message", "It's a match!"));
    } else {
      return ResponseEntity.ok(Map.of("message", "Like enregistré, en attente du like de l'autre utilisateur."));
    }
  }
}

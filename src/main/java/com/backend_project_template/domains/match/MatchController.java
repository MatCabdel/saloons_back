package com.backend_project_template.domains.match;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import com.backend_project_template.infrastructure.redis.RedisKeyBuilder;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  @GetMapping("/matches")
  public ResponseEntity<List<MatchUserDTO>> getMyMatches(Principal principal) {
    User me = userRepository.findByEmail(principal.getName()).orElseThrow();
    List<Match> matches = matchService.getMatchesForUser(me);
    LocalDateTime now = LocalDateTime.now();
    List<MatchUserDTO> matchedUsers = matches.stream()
        .map(m -> {
          User other = m.getUser1().equals(me) ? m.getUser2() : m.getUser1();
          LocalDateTime matchedAt = m.getMatchedAt();
          boolean sessionExpired = matchedAt != null
              && matchedAt.plusSeconds(RedisKeyBuilder.SESSION_TTL_SECONDS).isBefore(now);
          return new MatchUserDTO(other, matchedAt, sessionExpired);
        })
        .filter(user -> !user.getId().equals(me.getId()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(matchedUsers);
  }

  @GetMapping("/{userId1}/has-liked/{userId2}")
  public ResponseEntity<Map<String, Boolean>> hasLiked(@PathVariable Long userId1, @PathVariable Long userId2) {
    User u1 = userRepository.findById(userId1).orElseThrow();
    User u2 = userRepository.findById(userId2).orElseThrow();
    boolean hasLiked = matchService.hasLiked(u1, u2);
    return ResponseEntity.ok(Map.of("hasLiked", hasLiked));
  }
}

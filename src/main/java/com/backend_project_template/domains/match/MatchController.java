package com.backend_project_template.domains.match;

import com.backend_project_template.domains.session.SessionRedisService;
import com.backend_project_template.domains.conversation.ConversationRepository;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import com.backend_project_template.infrastructure.redis.RedisKeyBuilder;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/match")
public class MatchController {

  private static final int HEART_REQUEST_WINDOW_HOURS = 24;

  private final MatchService matchService;
  private final UserRepository userRepository;
  private final SessionRedisService sessionRedisService;
  private final MatchRepository matchRepository;
  private final ConversationRepository conversationRepository;

  public MatchController(MatchService matchService, UserRepository userRepository,
      SessionRedisService sessionRedisService, MatchRepository matchRepository,
      ConversationRepository conversationRepository) {
    this.matchService = matchService;
    this.userRepository = userRepository;
    this.sessionRedisService = sessionRedisService;
    this.matchRepository = matchRepository;
    this.conversationRepository = conversationRepository;
  }

  @PostMapping("/{userId1}/like/{userId2}")
  public ResponseEntity<?> likeUser(@PathVariable Long userId1, @PathVariable Long userId2, Principal principal) {
    // Sécurité : vérifier que l'utilisateur authentifié est bien userId1
    User authenticated = userRepository.findByEmail(principal.getName()).orElseThrow();
    if (!authenticated.getId().equals(userId1)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Accès refusé"));
    }
    // Sécurité : empêcher de se liker soi-même
    if (userId1.equals(userId2)) {
      return ResponseEntity.badRequest().body(Map.of("message", "Vous ne pouvez pas vous liker vous-même"));
    }
    // Sécurité : vérifier que les deux utilisateurs sont dans le même saloon
    Optional<Long> saloon1 = sessionRedisService.getSessionSaloonId(userId1);
    Optional<Long> saloon2 = sessionRedisService.getSessionSaloonId(userId2);
    if (saloon1.isEmpty() || saloon2.isEmpty() || !saloon1.get().equals(saloon2.get())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("message", "Les deux utilisateurs doivent être dans le même saloon"));
    }
    User u1 = authenticated;
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
          LocalDateTime scheduledEnd = matchedAt == null ? null
              : matchedAt.plusSeconds(RedisKeyBuilder.SESSION_TTL_SECONDS);
          LocalDateTime sessionEndedAt = m.getSessionEndedAt();
          Optional<Long> mySaloon = sessionRedisService.getSessionSaloonId(me.getId());
          Optional<Long> otherSaloon = sessionRedisService.getSessionSaloonId(other.getId());
          boolean bothStillActive = mySaloon.isPresent() && mySaloon.equals(otherSaloon);
          if (sessionEndedAt == null && scheduledEnd != null
              && (scheduledEnd.isBefore(now) || !bothStillActive)) {
            sessionEndedAt = scheduledEnd.isBefore(now) ? scheduledEnd : now;
            m.setSessionEndedAt(sessionEndedAt);
            matchRepository.save(m);
          }
          boolean sessionExpired = sessionEndedAt != null;
          LocalDateTime heartWindowExpiresAt = sessionEndedAt == null ? null
              : sessionEndedAt.plusHours(HEART_REQUEST_WINDOW_HOURS);
          boolean heartConfirmed = conversationRepository.findConversationBetweenUsers(me, other)
              .map(conversation -> conversation.isPermanent())
              .orElse(false);
          return new MatchUserDTO(other, matchedAt, sessionExpired, sessionEndedAt,
              heartWindowExpiresAt, heartConfirmed);
        })
        .filter(user -> user.isHeartConfirmed() || user.getHeartWindowExpiresAt() == null
            || user.getHeartWindowExpiresAt().isAfter(now))
        .filter(user -> !user.getId().equals(me.getId()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(matchedUsers);
  }

  @GetMapping("/{userId1}/has-liked/{userId2}")
  public ResponseEntity<Map<String, Boolean>> hasLiked(@PathVariable Long userId1, @PathVariable Long userId2,
      Principal principal) {
    // Sécurité : vérifier que l'utilisateur authentifié est bien userId1
    User authenticated = userRepository.findByEmail(principal.getName()).orElseThrow();
    if (!authenticated.getId().equals(userId1)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    User u1 = authenticated;
    User u2 = userRepository.findById(userId2).orElseThrow();
    boolean hasLiked = matchService.hasLiked(u1, u2);
    return ResponseEntity.ok(Map.of("hasLiked", hasLiked));
  }

  /**
   * Supprime un match avec un autre utilisateur.
   * Marque le match comme quitté par l'utilisateur courant.
   */
  @DeleteMapping("/{otherUserId}")
  public ResponseEntity<Void> deleteMatch(@PathVariable Long otherUserId, Principal principal) {
    User me = userRepository.findByEmail(principal.getName()).orElseThrow();
    User other = userRepository.findById(otherUserId).orElseThrow();
    matchService.leaveMatch(me, other);
    return ResponseEntity.noContent().build();
  }
}

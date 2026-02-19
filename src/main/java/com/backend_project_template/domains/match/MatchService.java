package com.backend_project_template.domains.match;

import com.backend_project_template.domains.pushtoken.FcmNotificationService;
import com.backend_project_template.domains.user.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MatchService.class);

  private final MatchRepository matchRepository;
  private final UserLikeRepository userLikeRepository;
  private final FcmNotificationService fcmNotificationService;

  public MatchService(MatchRepository matchRepository, UserLikeRepository userLikeRepository,
      FcmNotificationService fcmNotificationService) {
    this.matchRepository = matchRepository;
    this.userLikeRepository = userLikeRepository;
    this.fcmNotificationService = fcmNotificationService;
  }

  /**
   * User "liker" envoie un clin d'œil à user "liked".
   * Si "liked" avait déjà liké "liker", un match est créé et on notifie "liked"
   * (l'initiateur du premier clin d'œil).
   * 
   * @return true si c'est un match (réciproque), false sinon
   */
  public boolean like(User liker, User liked) {
    if (!userLikeRepository.existsByLikerAndLiked(liker, liked)) {
      userLikeRepository.save(new UserLike(liker, liked));
    }
    
    // Vérifier si l'autre avait déjà liké (donc match réciproque)
    if (userLikeRepository.existsByLikerAndLiked(liked, liker)) {
      boolean matchCreated = false;
      if (!matchRepository.existsByUser1AndUser2(liker, liked)
          && !matchRepository.existsByUser1AndUser2(liked, liker)) {
        Match match = matchRepository.save(new Match(liker, liked));
        matchCreated = true;
        LOGGER.info("💕 [match_detected] matchId={}, user1={}, user2={}", 
            match.getId(), liker.getId(), liked.getId());
        
        // Envoyer push UNIQUEMENT à "liked" (celui qui avait liké en premier = initiateur)
        // "liker" vient de liker, donc il sait déjà. "liked" doit être notifié.
        sendMatchPushNotification(liked, liker);
      }
      return true;
    }
    return false;
  }

  public boolean isMatched(User u1, User u2) {
    return matchRepository.existsByUser1AndUser2(u1, u2) || matchRepository.existsByUser2AndUser1(u1, u2);
  }

  public Match createMatch(User u1, User u2) {
    if (!isMatched(u1, u2)) {
      return matchRepository.save(new Match(u1, u2));
    }
    return null;
  }

  /**
   * Retourne les matches actifs pour un utilisateur (où il n'a pas quitté).
   */
  public List<Match> getMatchesForUser(User user) {
    return matchRepository.findActiveMatchesForUser(user);
  }

  public boolean hasLiked(User liker, User liked) {
    return userLikeRepository.existsByLikerAndLiked(liker, liked);
  }

  /**
   * Marque un match comme quitté par un utilisateur.
   */
  public boolean leaveMatch(User user, User otherUser) {
    Optional<Match> matchOpt = matchRepository.findMatchBetweenUsers(user, otherUser);
    if (matchOpt.isPresent()) {
      Match match = matchOpt.get();
      match.markAsLeftBy(user);
      matchRepository.save(match);
      return true;
    }
    return false;
  }

  /**
   * Vérifie si l'autre utilisateur a quitté le match.
   */
  public boolean hasOtherUserLeft(User currentUser, User otherUser) {
    Optional<Match> matchOpt = matchRepository.findMatchBetweenUsers(currentUser, otherUser);
    return matchOpt.map(m -> m.hasOtherUserLeft(currentUser)).orElse(false);
  }

  /**
   * Vérifie si l'utilisateur courant a quitté le match (l'autre ne peut plus
   * accéder).
   */
  public boolean hasUserLeft(User currentUser, User otherUser) {
    Optional<Match> matchOpt = matchRepository.findMatchBetweenUsers(currentUser, otherUser);
    return matchOpt.map(m -> m.hasLeftForUser(currentUser)).orElse(false);
  }

  /**
   * Vérifie si l'accès au profil est bloqué (l'un des deux a quitté).
   */
  public boolean isAccessBlocked(User currentUser, User otherUser) {
    Optional<Match> matchOpt = matchRepository.findMatchBetweenUsers(currentUser, otherUser);
    if (matchOpt.isEmpty()) {
      return false; // Pas de match, accès libre (profil public dans saloon)
    }
    Match match = matchOpt.get();
    // Bloqué si l'autre a quitté OU si l'utilisateur courant a quitté
    return match.hasOtherUserLeft(currentUser) || match.hasLeftForUser(currentUser);
  }

  /**
   * Envoie une notification push de match à l'utilisateur qui avait initié le
   * premier clin d'œil.
   * 
   * @param recipient L'utilisateur qui reçoit la notification (initiateur du
   *                  premier wink)
   * @param matcher   L'utilisateur qui vient de renvoyer le wink (a créé le match)
   */
  private void sendMatchPushNotification(User recipient, User matcher) {
    LOGGER.info("💕 [match_push_target_user] recipientId={}, recipientName={}", 
        recipient.getId(), recipient.getUserName());

    try {
      String title = "Saloons";
      String body = "Clin d'œil réciproque — c'est un match !";

      Map<String, String> data = new HashMap<>();
      data.put("type", "match");
      data.put("matcherId", String.valueOf(matcher.getId()));
      data.put("matcherName", matcher.getUserName() != null ? matcher.getUserName() : "");

      fcmNotificationService.sendToUser(recipient.getId(), title, body, data);
      LOGGER.info("💕 [match_push_sent] recipientId={}", recipient.getId());
    } catch (Exception e) {
      LOGGER.error("💕 [match_push_error] recipientId={}, error={}", recipient.getId(), e.getMessage(), e);
    }
  }
}

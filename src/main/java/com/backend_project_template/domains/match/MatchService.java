package com.backend_project_template.domains.match;

import com.backend_project_template.domains.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

  private final MatchRepository matchRepository;
  private final UserLikeRepository userLikeRepository;

  public MatchService(MatchRepository matchRepository, UserLikeRepository userLikeRepository) {
    this.matchRepository = matchRepository;
    this.userLikeRepository = userLikeRepository;
  }

  public boolean like(User liker, User liked) {
    if (!userLikeRepository.existsByLikerAndLiked(liker, liked)) {
      userLikeRepository.save(new UserLike(liker, liked));
    }
    if (userLikeRepository.existsByLikerAndLiked(liked, liker)) {
      if (!matchRepository.existsByUser1AndUser2(liker, liked)
          && !matchRepository.existsByUser1AndUser2(liked, liker)) {
        matchRepository.save(new Match(liker, liked));
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
}

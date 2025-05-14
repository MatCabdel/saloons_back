package com.backend_project_template.domains.match;

import com.backend_project_template.Entity.User;
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
      if (!matchRepository.existsByUser1AndUser2(liker, liked) && !matchRepository.existsByUser1AndUser2(liked, liker)) {
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
}

package com.backend_project_template.domains.match;

import com.backend_project_template.domains.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

  /**
   * Trouve les matches actifs pour un utilisateur où PERSONNE n'a quitté.
   * Utilisé pour les "new winks".
   */
  @Query("SELECT m FROM Match m WHERE "
      + "(m.user1 = :user OR m.user2 = :user) "
      + "AND m.leftByUser1At IS NULL AND m.leftByUser2At IS NULL")
  List<Match> findActiveMatchesForUser(@Param("user") User user);

  /**
   * Trouve tous les matches pour un utilisateur (y compris ceux qu'il a quittés).
   */
  List<Match> findByUser1OrUser2(User user1, User user2);

  boolean existsByUser1AndUser2(User user1, User user2);

  boolean existsByUser2AndUser1(User user1, User user2);

  /**
   * Trouve un match entre deux utilisateurs.
   */
  @Query("SELECT m FROM Match m WHERE "
      + "(m.user1 = :user1 AND m.user2 = :user2) OR "
      + "(m.user1 = :user2 AND m.user2 = :user1)")
  Optional<Match> findMatchBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

  void deleteByUser1(User user1);

  void deleteByUser2(User user2);
}

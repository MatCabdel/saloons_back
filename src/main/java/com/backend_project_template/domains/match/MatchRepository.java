package com.backend_project_template.domains.match;

import com.backend_project_template.Entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
  List<Match> findByUser1OrUser2(User user1, User user2);
  boolean existsByUser1AndUser2(User user1, User user2);
  boolean existsByUser2AndUser1(User user1, User user2);
}

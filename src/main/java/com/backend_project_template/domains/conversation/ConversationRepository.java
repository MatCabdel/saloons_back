package com.backend_project_template.domains.conversation;

import com.backend_project_template.domains.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
  List<Conversation> findByParticipantsContaining(User user);

  @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 WHERE p1 = :user1 AND p2 = :user2")
  Optional<Conversation> findConversationBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

  @Modifying
  @Query(value = "DELETE FROM conversation_participants WHERE user_id = :userId", nativeQuery = true)
  void deleteParticipantsByUserId(@Param("userId") Long userId);
}

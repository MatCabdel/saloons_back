package com.backend_project_template.domains.conversation;

import com.backend_project_template.domains.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

  /**
   * Trouve les conversations où l'utilisateur est participant, n'a pas quitté,
   * et qui ont au moins un message.
   */
  @Query("SELECT DISTINCT c FROM Conversation c "
      + "JOIN c.conversationParticipants cp "
      + "JOIN c.messages m "
      + "WHERE cp.user = :user AND cp.leftAt IS NULL")
  List<Conversation> findActiveConversationsForUser(@Param("user") User user);

  /**
   * Trouve une conversation entre deux utilisateurs (peu importe s'ils ont quitté
   * ou non).
   */
  @Query("SELECT c FROM Conversation c "
      + "JOIN c.conversationParticipants cp1 JOIN c.conversationParticipants cp2 "
      + "WHERE cp1.user = :user1 AND cp2.user = :user2")
  Optional<Conversation> findConversationBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
}

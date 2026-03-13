package com.backend_project_template.domains.message;

import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.user.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);

  List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

  void deleteBySender(User sender);

  // ============ STATS QUERIES ============

  /**
   * Messages envoyés dans une période.
   */
  @Query("SELECT COUNT(m) FROM Message m WHERE m.sentAt BETWEEN :from AND :to")
  long countMessagesBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Moyenne de messages par conversation dans une période.
   */
  @Query("SELECT AVG(cnt) FROM ("
      + "SELECT COUNT(m) as cnt FROM Message m "
      + "WHERE m.sentAt BETWEEN :from AND :to "
      + "GROUP BY m.conversation.id) sub")
  Double avgMessagesPerConversation(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Messages par jour dans une période.
   */
  @Query("SELECT DATE(m.sentAt), COUNT(m) FROM Message m "
      + "WHERE m.sentAt BETWEEN :from AND :to "
      + "GROUP BY DATE(m.sentAt) ORDER BY DATE(m.sentAt)")
  List<Object[]> countMessagesPerDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

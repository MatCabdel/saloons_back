package com.backend_project_template.domains.heartRequest;

import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HeartRequestRepository extends JpaRepository<HeartRequest, Long> {

    Optional<HeartRequest> findBySenderAndConversation(User sender, Conversation conversation);

    Optional<HeartRequest> findByReceiverAndConversation(User receiver, Conversation conversation);

    List<HeartRequest> findByConversationId(Long conversationId);

    boolean existsBySenderAndReceiverAndConversation(User sender, User receiver, Conversation conversation);

    List<HeartRequest> findBySenderId(Long senderId);

    List<HeartRequest> findByReceiverId(Long receiverId);

    // ============ STATS QUERIES ============

    /**
     * Coups de cœur envoyés dans une période.
     */
    @Query("SELECT COUNT(hr) FROM HeartRequest hr WHERE hr.createdAt BETWEEN :from AND :to")
    long countHeartRequestsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Coups de cœur par jour dans une période.
     */
    @Query("SELECT DATE(hr.createdAt), COUNT(hr) FROM HeartRequest hr "
            + "WHERE hr.createdAt BETWEEN :from AND :to "
            + "GROUP BY DATE(hr.createdAt) ORDER BY DATE(hr.createdAt)")
    List<Object[]> countHeartRequestsPerDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

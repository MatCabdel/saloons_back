package com.backend_project_template.domains.conversation;

import com.backend_project_template.domains.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    Optional<ConversationParticipant> findByConversationAndUser(Conversation conversation, User user);

    @Modifying
    @Query("DELETE FROM ConversationParticipant cp WHERE cp.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}

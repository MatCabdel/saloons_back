package com.backend_project_template.domains.message;

import com.backend_project_template.domains.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);
}

package com.backend_project_template.domains.message;

import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);

  List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

  void deleteBySender(User sender);
}

package com.backend_project_template.domains.conversation;

import com.backend_project_template.Entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
  List<Conversation> findByParticipantsContaining(User user);
}

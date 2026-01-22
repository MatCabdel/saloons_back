package com.backend_project_template.domains.heartRequest;

import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}

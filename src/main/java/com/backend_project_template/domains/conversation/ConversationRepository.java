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
     * Trouve une conversation par ID avec ses participants chargés (FETCH JOIN).
     * Évite LazyInitializationException lors de l'accès aux participants hors
     * session.
     */
    @Query("SELECT c FROM Conversation c "
            + "LEFT JOIN FETCH c.conversationParticipants cp "
            + "LEFT JOIN FETCH cp.user "
            + "WHERE c.id = :conversationId")
    Optional<Conversation> findByIdWithParticipants(@Param("conversationId") Long conversationId);

    /**
     * Trouve les conversations où l'utilisateur est participant et qui ont au moins
     * un message.
     * Inclut les conversations expirées (leftAt non null) pour permettre les coups
     * de cœur.
     * Exclut uniquement si le match a été annulé (géré côté service).
     */
    @Query("SELECT DISTINCT c FROM Conversation c "
            + "JOIN c.conversationParticipants cp "
            + "JOIN c.messages m "
            + "WHERE cp.user = :user")
    List<Conversation> findAllConversationsForUser(@Param("user") User user);

    /**
     * Trouve une conversation entre deux utilisateurs (peu importe s'ils ont quitté
     * ou non).
     */
    @Query("SELECT c FROM Conversation c "
            + "JOIN c.conversationParticipants cp1 JOIN c.conversationParticipants cp2 "
            + "WHERE cp1.user = :user1 AND cp2.user = :user2")
    Optional<Conversation> findConversationBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Trouve les conversations actives (non permanentes, non quittées) d'un
     * utilisateur
     * dans un saloon spécifique.
     */
    @Query("SELECT c FROM Conversation c "
            + "JOIN c.conversationParticipants cp "
            + "WHERE cp.user.id = :userId "
            + "AND c.saloon.id = :saloonId "
            + "AND cp.leftAt IS NULL "
            + "AND c.isPermanent = false")
    List<Conversation> findActiveConversationsForUserInSaloon(
            @Param("userId") Long userId,
            @Param("saloonId") Long saloonId);
}

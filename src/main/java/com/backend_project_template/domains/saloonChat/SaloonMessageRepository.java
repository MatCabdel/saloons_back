package com.backend_project_template.domains.saloonChat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaloonMessageRepository extends JpaRepository<SaloonMessage, Long> {

    @Query("SELECT m FROM SaloonMessage m WHERE m.saloon.id = :saloonId ORDER BY m.createdAt DESC")
    List<SaloonMessage> findBySaloonIdOrderByCreatedAtDesc(@Param("saloonId") Long saloonId, Pageable pageable);

    @Query("SELECT m FROM SaloonMessage m WHERE m.saloon.id = :saloonId ORDER BY m.createdAt ASC")
    List<SaloonMessage> findBySaloonIdOrderByCreatedAtAsc(@Param("saloonId") Long saloonId);

    /**
     * Récupère les messages d'un saloon créés après une date donnée (pour filtrer
     * par joinedAt de l'utilisateur)
     */
    @Query("SELECT m FROM SaloonMessage m WHERE m.saloon.id = :saloonId AND m.createdAt >= :since ORDER BY m.createdAt ASC")
    List<SaloonMessage> findBySaloonIdAndCreatedAtAfterOrderByCreatedAtAsc(
            @Param("saloonId") Long saloonId,
            @Param("since") LocalDateTime since);

    /**
     * Récupère les messages d'un saloon créés après une date donnée avec limite
     */
    @Query("SELECT m FROM SaloonMessage m WHERE m.saloon.id = :saloonId AND m.createdAt >= :since ORDER BY m.createdAt ASC")
    List<SaloonMessage> findBySaloonIdAndCreatedAtAfterOrderByCreatedAtAsc(
            @Param("saloonId") Long saloonId,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    void deleteBySaloonId(Long saloonId);

    void deleteBySender(com.backend_project_template.domains.user.User sender);

    // Purge des messages plus anciens que la date donnée (TTL)
    @Modifying
    @Query("DELETE FROM SaloonMessage m WHERE m.createdAt < :cutoffDate")
    int deleteMessagesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}

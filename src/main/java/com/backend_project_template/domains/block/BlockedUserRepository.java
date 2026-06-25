package com.backend_project_template.domains.block;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    Optional<BlockedUser> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    List<BlockedUser> findAllByOrderByCreatedAtDesc();

    /**
     * Récupère les IDs des utilisateurs bloqués par userId.
     */
    @Query("SELECT b.blocked.id FROM BlockedUser b WHERE b.blocker.id = :userId")
    Set<Long> findBlockedIdsByBlockerId(@Param("userId") Long userId);

    /**
     * Vérifie si A bloque B ou B bloque A (blocage mutuel).
     */
    @Query("SELECT COUNT(b) > 0 FROM BlockedUser b WHERE " +
           "(b.blocker.id = :userId1 AND b.blocked.id = :userId2) OR " +
           "(b.blocker.id = :userId2 AND b.blocked.id = :userId1)")
    boolean existsMutualBlock(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Récupère les IDs de tous les utilisateurs avec qui userId a un blocage mutuel
     * (il les bloque ou ils le bloquent).
     */
    @Query(value =
           "SELECT blocked_user_id FROM blocked_users WHERE blocker_user_id = :userId " +
           "UNION " +
           "SELECT blocker_user_id FROM blocked_users WHERE blocked_user_id = :userId",
           nativeQuery = true)
    Set<Long> findMutuallyBlockedIds(@Param("userId") Long userId);
}

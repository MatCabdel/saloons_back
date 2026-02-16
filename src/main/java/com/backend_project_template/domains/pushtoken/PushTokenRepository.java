package com.backend_project_template.domains.pushtoken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    /**
     * Trouve un token par sa valeur
     */
    Optional<PushToken> findByToken(String token);

    /**
     * Trouve tous les tokens actifs d'un utilisateur
     */
    List<PushToken> findByUserIdAndActiveTrue(Long userId);

    /**
     * Trouve tous les tokens actifs d'une liste d'utilisateurs
     */
    @Query("SELECT pt FROM PushToken pt WHERE pt.user.id IN :userIds AND pt.active = true")
    List<PushToken> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * Désactive un token (au lieu de le supprimer)
     */
    @Modifying
    @Query("UPDATE PushToken pt SET pt.active = false WHERE pt.token = :token")
    int deactivateToken(@Param("token") String token);

    /**
     * Supprime tous les tokens d'un utilisateur
     */
    void deleteByUserId(Long userId);

    /**
     * Vérifie si un token existe déjà
     */
    boolean existsByToken(String token);
}

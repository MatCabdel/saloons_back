package com.backend_project_template.domains.subscription;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PremiumSubscriptionRepository extends JpaRepository<PremiumSubscription, Long> {

    /**
     * Compte les abonnements actifs à une date donnée
     */
    @Query("SELECT COUNT(p) FROM PremiumSubscription p WHERE p.startDate <= :date AND (p.endDate IS NULL OR p.endDate >= :date) AND p.isActive = true")
    long countActiveAtDate(@Param("date") LocalDateTime date);

    /**
     * Compte les abonnements créés dans un mois donné
     */
    @Query("SELECT COUNT(p) FROM PremiumSubscription p WHERE YEAR(p.startDate) = :year AND MONTH(p.startDate) = :month")
    long countByMonth(@Param("year") int year, @Param("month") int month);

    /**
     * Trouve tous les abonnements d'un utilisateur
     */
    List<PremiumSubscription> findByUserId(Long userId);

    /**
     * Supprime tous les abonnements d'un utilisateur
     */
    void deleteByUser(com.backend_project_template.domains.user.User user);

    /**
     * Compte les abonnements actifs actuellement
     */
    @Query("SELECT COUNT(p) FROM PremiumSubscription p WHERE p.isActive = true AND (p.endDate IS NULL OR p.endDate >= CURRENT_TIMESTAMP)")
    long countCurrentlyActive();
}

package com.backend_project_template.domains.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findById(Long id);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  /**
   * Récupère tous les utilisateurs avec pagination
   */
  Page<User> findAll(Pageable pageable);

  /**
   * Compte les utilisateurs actifs (connectés après une date donnée)
   */
  long countByLastLoginAtAfter(LocalDateTime date);

  /**
   * Compte les utilisateurs premium actifs
   */
  long countByIsPremiumTrue();

  /**
   * Compte les utilisateurs par ville
   */
  @Query("SELECT u.city, COUNT(u) FROM User u WHERE u.city IS NOT NULL AND u.city <> '' GROUP BY u.city")
  List<Object[]> countByCity();

  /**
   * Compte les utilisateurs actuellement connectés dans un saloon par ville
   */
  @Query("SELECT u.city, COUNT(u) FROM User u WHERE u.currentSaloon IS NOT NULL AND u.city IS NOT NULL AND u.city <> '' GROUP BY u.city")
  List<Object[]> countConnectedByCity();

  /**
   * Recherche des utilisateurs par nom, prénom, pseudo ou email avec pagination
   */
  @Query("SELECT u FROM User u WHERE "
      + "LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
      + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
      + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
      + "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<User> searchUsers(@Param("search") String search, Pageable pageable);

  // ============ STATS QUERIES ============

  /**
   * Nouveaux inscrits par jour dans une période.
   */
  @Query("SELECT DATE(u.createdAt), COUNT(u) FROM User u "
      + "WHERE u.createdAt BETWEEN :from AND :to "
      + "GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt)")
  List<Object[]> countNewUsersPerDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Nombre total d'inscrits dans une période.
   */
  long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

  /**
   * Rétention : utilisateurs créés dans la période fromReg/toReg qui se sont connectés après retentionDate.
   */
  @Query("SELECT COUNT(u) FROM User u "
      + "WHERE u.createdAt BETWEEN :fromReg AND :toReg "
      + "AND u.lastLoginAt >= :retentionDate")
  long countRetainedUsers(
      @Param("fromReg") LocalDateTime fromReg,
      @Param("toReg") LocalDateTime toReg,
      @Param("retentionDate") LocalDateTime retentionDate);

  /**
   * Churn : utilisateurs dont le dernier login est avant churnDate.
   */
  @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt IS NOT NULL AND u.lastLoginAt < :churnDate")
  long countChurnedUsers(@Param("churnDate") LocalDateTime churnDate);

  /**
   * Utilisateurs premium par ville.
   */
  @Query("SELECT u.city, COUNT(u) FROM User u "
      + "WHERE u.isPremium = true AND u.city IS NOT NULL AND u.city <> '' "
      + "GROUP BY u.city ORDER BY COUNT(u) DESC")
  List<Object[]> countPremiumByCity();

  /**
   * Utilisateurs qui n'ont jamais été dans un saloon (aucune session).
   */
  @Query("SELECT COUNT(DISTINCT u) FROM User u "
      + "WHERE u.id NOT IN (SELECT DISTINCT ss.user.id FROM SaloonSession ss)")
  long countUsersNeverInSaloon();

  /**
   * Utilisateurs avec profil complété.
   */
  @Query("SELECT COUNT(u) FROM User u WHERE u.profileStatus = 'PROFILE_COMPLETE'")
  long countProfileComplete();

  /**
   * Utilisateurs actifs (connectés dans les 7 derniers jours) par ville.
   */
  @Query("SELECT u.city, COUNT(u) FROM User u "
      + "WHERE u.city IS NOT NULL AND u.city <> '' "
      + "AND u.lastLoginAt >= :since "
      + "GROUP BY u.city ORDER BY COUNT(u) DESC")
  List<Object[]> countActiveUsersByCity(@Param("since") LocalDateTime since);

  /**
   * Évolution des utilisateurs actifs (connectés dans les 7j précédant ce mois)
   * par mois et par ville sur les 12 derniers mois.
   * Retourne : année, mois, ville, nb actifs
   */
  @Query("SELECT YEAR(u.lastLoginAt), MONTH(u.lastLoginAt), u.city, COUNT(u) "
      + "FROM User u "
      + "WHERE u.city IS NOT NULL AND u.city <> '' "
      + "AND u.lastLoginAt >= :since "
      + "GROUP BY YEAR(u.lastLoginAt), MONTH(u.lastLoginAt), u.city "
      + "ORDER BY YEAR(u.lastLoginAt), MONTH(u.lastLoginAt)")
  List<Object[]> countActiveUsersByMonthAndCity(@Param("since") LocalDateTime since);
}

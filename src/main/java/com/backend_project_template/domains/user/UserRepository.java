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
}

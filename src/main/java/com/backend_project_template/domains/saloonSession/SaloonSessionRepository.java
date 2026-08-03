package com.backend_project_template.domains.saloonSession;

import com.backend_project_template.domains.user.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaloonSessionRepository extends JpaRepository<SaloonSession, Long> {
  SaloonSession findFirstByUserIdAndDisconnectedAtIsNull(Long userId);

  SaloonSession findFirstByUserIdAndSaloonIdAndDisconnectedAtIsNullOrderByConnectedAtDesc(
      Long userId, Long saloonId);

  void deleteByUser(User user);

  long countBySaloonIdAndConnectedAtBetween(Long saloonId, LocalDateTime from, LocalDateTime to);

  long countBySaloonId(Long saloonId);

  @Query("SELECT COUNT(DISTINCT ss.user.id) FROM SaloonSession ss WHERE ss.saloon.id = :saloonId")
  long countUniqueVisitorsForSaloon(@Param("saloonId") Long saloonId);

  @Query("SELECT COUNT(DISTINCT ss.user.id) FROM SaloonSession ss "
      + "WHERE ss.saloon.id = :saloonId AND ss.connectedAt BETWEEN :from AND :to")
  long countUniqueVisitorsForSaloonBetween(@Param("saloonId") Long saloonId,
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  @Query("SELECT MIN(ss.connectedAt) FROM SaloonSession ss WHERE ss.saloon.id = :saloonId")
  LocalDateTime findFirstConnectionDateForSaloon(@Param("saloonId") Long saloonId);

  @Query("SELECT HOUR(ss.connectedAt), COUNT(ss) FROM SaloonSession ss "
      + "WHERE ss.saloon.id = :saloonId AND ss.connectedAt BETWEEN :from AND :to "
      + "GROUP BY HOUR(ss.connectedAt) ORDER BY HOUR(ss.connectedAt)")
  List<Object[]> countEntriesByHourForSaloon(@Param("saloonId") Long saloonId,
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  @Query("SELECT DATE(ss.connectedAt), COUNT(ss) FROM SaloonSession ss "
      + "WHERE ss.saloon.id = :saloonId AND ss.connectedAt BETWEEN :from AND :to "
      + "GROUP BY DATE(ss.connectedAt) ORDER BY DATE(ss.connectedAt)")
  List<Object[]> countEntriesByDayForSaloon(@Param("saloonId") Long saloonId,
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  @Query("SELECT MONTH(ss.connectedAt), COUNT(ss) FROM SaloonSession ss "
      + "WHERE ss.saloon.id = :saloonId AND ss.connectedAt BETWEEN :from AND :to "
      + "GROUP BY MONTH(ss.connectedAt) ORDER BY MONTH(ss.connectedAt)")
  List<Object[]> countEntriesByMonthForSaloon(@Param("saloonId") Long saloonId,
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  @Query("SELECT DAYOFWEEK(ss.connectedAt), COUNT(ss) FROM SaloonSession ss "
      + "WHERE ss.saloon.id = :saloonId AND ss.connectedAt BETWEEN :from AND :to "
      + "GROUP BY DAYOFWEEK(ss.connectedAt) ORDER BY DAYOFWEEK(ss.connectedAt)")
  List<Object[]> countEntriesByWeekDayForSaloon(@Param("saloonId") Long saloonId,
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  @Query("SELECT DAYOFWEEK(ss.connectedAt), HOUR(ss.connectedAt), COUNT(ss) "
      + "FROM SaloonSession ss "
      + "WHERE ss.saloon.id = :saloonId AND ss.connectedAt BETWEEN :from AND :to "
      + "GROUP BY DAYOFWEEK(ss.connectedAt), HOUR(ss.connectedAt) "
      + "ORDER BY DAYOFWEEK(ss.connectedAt), HOUR(ss.connectedAt)")
  List<Object[]> countEntriesByWeekDayAndHourForSaloon(@Param("saloonId") Long saloonId,
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  // ============ STATS QUERIES ============

  /**
   * Entrées dans un saloon par jour.
   */
  @Query("SELECT DATE(ss.connectedAt), COUNT(ss) FROM SaloonSession ss "
      + "WHERE ss.connectedAt BETWEEN :from AND :to "
      + "GROUP BY DATE(ss.connectedAt) ORDER BY DATE(ss.connectedAt)")
  List<Object[]> countEntriesPerDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Nombre total d'entrées dans une période.
   */
  @Query("SELECT COUNT(ss) FROM SaloonSession ss WHERE ss.connectedAt BETWEEN :from AND :to")
  long countEntriesBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Utilisateurs uniques ayant visité un saloon dans une période.
   */
  @Query("SELECT COUNT(DISTINCT ss.user.id) FROM SaloonSession ss "
      + "WHERE ss.connectedAt BETWEEN :from AND :to")
  long countUniqueVisitorsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Top saloons par nombre d'entrées dans une période.
   */
  @Query("SELECT ss.saloon.id, ss.saloon.name, COUNT(ss) FROM SaloonSession ss "
      + "WHERE ss.connectedAt BETWEEN :from AND :to "
      + "GROUP BY ss.saloon.id, ss.saloon.name "
      + "ORDER BY COUNT(ss) DESC")
  List<Object[]> topSaloonsByEntries(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Moyenne de saloons visités par utilisateur dans une période.
   */
  @Query("SELECT AVG(cnt) FROM ("
      + "SELECT COUNT(DISTINCT ss.saloon.id) as cnt FROM SaloonSession ss "
      + "WHERE ss.connectedAt BETWEEN :from AND :to "
      + "GROUP BY ss.user.id) sub")
  Double avgSaloonsPerUser(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Activité par heure de la journée dans une période.
   */
  @Query("SELECT HOUR(ss.connectedAt), COUNT(ss) FROM SaloonSession ss "
      + "WHERE ss.connectedAt BETWEEN :from AND :to "
      + "GROUP BY HOUR(ss.connectedAt) ORDER BY HOUR(ss.connectedAt)")
  List<Object[]> countEntriesByHour(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Activité par jour de la semaine dans une période (1=dimanche, 7=samedi en
   * MySQL).
   */
  @Query("SELECT DAYOFWEEK(ss.connectedAt), HOUR(ss.connectedAt), COUNT(ss) FROM SaloonSession ss "
      + "WHERE ss.connectedAt BETWEEN :from AND :to "
      + "GROUP BY DAYOFWEEK(ss.connectedAt), HOUR(ss.connectedAt) "
      + "ORDER BY DAYOFWEEK(ss.connectedAt), HOUR(ss.connectedAt)")
  List<Object[]> countEntriesByDayAndHour(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Entrées par ville dans une période.
   */
  @Query("SELECT ss.saloon.city, COUNT(ss) FROM SaloonSession ss "
      + "WHERE ss.connectedAt BETWEEN :from AND :to "
      + "AND ss.saloon.city IS NOT NULL AND ss.saloon.city <> '' "
      + "GROUP BY ss.saloon.city ORDER BY COUNT(ss) DESC")
  List<Object[]> countEntriesByCity(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Utilisateurs uniques par saloon dans une période.
   */
  @Query("SELECT ss.saloon.id, ss.saloon.name, COUNT(DISTINCT ss.user.id) FROM SaloonSession ss "
      + "WHERE ss.connectedAt BETWEEN :from AND :to "
      + "GROUP BY ss.saloon.id, ss.saloon.name "
      + "ORDER BY COUNT(DISTINCT ss.user.id) DESC")
  List<Object[]> uniqueVisitorsPerSaloon(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  /**
   * Nombre total de visites par saloon (tous temps).
   */
  @Query("SELECT ss.saloon.id, COUNT(ss) FROM SaloonSession ss "
      + "GROUP BY ss.saloon.id")
  List<Object[]> countTotalVisitsPerSaloon();

  /**
   * Pic de connexions simultanées par saloon : nombre maximum de sessions
   * chevauchantes en même temps, approximé par minute.
   * On compte pour chaque session le nombre d'autres sessions actives
   * au moment de sa connexion (connecté avant lui, pas encore déconnecté).
   */
  @Query(value = "SELECT outer_ss.saloon_id, MAX(outer_ss.overlap_count) FROM ("
      + "SELECT a.saloon_id, ("
      + "SELECT COUNT(*) FROM saloon_sessions b "
      + "WHERE b.saloon_id = a.saloon_id "
      + "AND b.connected_at <= a.connected_at "
      + "AND (b.disconnected_at IS NULL OR b.disconnected_at >= a.connected_at)"
      + ") AS overlap_count "
      + "FROM saloon_sessions a) outer_ss "
      + "GROUP BY outer_ss.saloon_id", nativeQuery = true)
  List<Object[]> peakConcurrentUsersPerSaloon();
}

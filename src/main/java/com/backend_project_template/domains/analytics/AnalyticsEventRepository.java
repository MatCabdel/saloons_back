package com.backend_project_template.domains.analytics;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    long countByEventTypeAndCreatedAtBetween(
            AnalyticsEventType eventType, LocalDateTime from, LocalDateTime to);

    @Query("SELECT DATE(e.createdAt), COUNT(e) FROM AnalyticsEvent e "
            + "WHERE e.eventType = :type AND e.createdAt BETWEEN :from AND :to "
            + "GROUP BY DATE(e.createdAt) ORDER BY DATE(e.createdAt)")
    List<Object[]> countByTypeGroupedByDay(
            @Param("type") AnalyticsEventType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("SELECT e.city, COUNT(e) FROM AnalyticsEvent e "
            + "WHERE e.eventType = :type AND e.createdAt BETWEEN :from AND :to "
            + "AND e.city IS NOT NULL "
            + "GROUP BY e.city ORDER BY COUNT(e) DESC")
    List<Object[]> countByTypeGroupedByCity(
            @Param("type") AnalyticsEventType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}

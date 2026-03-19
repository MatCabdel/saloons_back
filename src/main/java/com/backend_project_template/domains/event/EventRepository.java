package com.backend_project_template.domains.event;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Récupère les événements actifs dont la date de début est dans la période
     * et dont la date n'est pas passée, triés par date de début.
     */
    @Query("SELECT e FROM Event e WHERE e.isActive = true "
            + "AND e.startDateTime BETWEEN :from AND :to "
            + "AND e.startDateTime >= :startOfToday "
            + "ORDER BY e.startDateTime ASC")
    List<Event> findActiveByPeriod(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("startOfToday") LocalDateTime startOfToday);

    /**
     * Récupère les événements actifs par période avec pagination.
     */
    @Query("SELECT e FROM Event e WHERE e.isActive = true "
            + "AND e.startDateTime BETWEEN :from AND :to "
            + "AND e.startDateTime >= :startOfToday")
    Page<Event> findActiveByPeriodPaged(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("startOfToday") LocalDateTime startOfToday,
            Pageable pageable);

    /**
     * Récupère tous les événements actifs dont la date n'est pas passée,
     * triés par date de début.
     */
    @Query("SELECT e FROM Event e WHERE e.isActive = true "
            + "AND e.startDateTime >= :startOfToday "
            + "ORDER BY e.startDateTime ASC")
    List<Event> findAllActiveNotPast(
            @Param("startOfToday") LocalDateTime startOfToday);

    /**
     * Récupère tous les événements actifs non passés avec pagination.
     */
    @Query("SELECT e FROM Event e WHERE e.isActive = true "
            + "AND e.startDateTime >= :startOfToday")
    Page<Event> findAllActiveNotPastPaged(
            @Param("startOfToday") LocalDateTime startOfToday,
            Pageable pageable);

    /**
     * Récupère tous les événements avec pagination.
     */
    Page<Event> findAll(Pageable pageable);

    /**
     * Recherche des événements par titre avec pagination.
     */
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Event> searchEvents(@Param("search") String search, Pageable pageable);

    /**
     * Compte les événements actifs.
     */
    long countByIsActiveTrue();

    /**
     * Récupère les événements actifs par période avec pagination,
     * filtrés par distance au saloon (formule de Haversine, rayon terrestre 6 371 km).
     */
    @Query(value = "SELECT e.* FROM event e "
            + "JOIN saloon s ON e.saloon_id = s.id "
            + "WHERE e.is_active = true "
            + "AND e.start_date_time BETWEEN :fromDt AND :toDt "
            + "AND e.start_date_time >= :startOfToday "
            + "AND (6371000 * ACOS("
            + "  COS(RADIANS(:lat)) * COS(RADIANS(s.latitude)) * "
            + "  COS(RADIANS(s.longitude) - RADIANS(:lng)) + "
            + "  SIN(RADIANS(:lat)) * SIN(RADIANS(s.latitude))"
            + ")) <= :maxDist "
            + "ORDER BY e.start_date_time ASC",
        countQuery = "SELECT COUNT(*) FROM event e "
            + "JOIN saloon s ON e.saloon_id = s.id "
            + "WHERE e.is_active = true "
            + "AND e.start_date_time BETWEEN :fromDt AND :toDt "
            + "AND e.start_date_time >= :startOfToday "
            + "AND (6371000 * ACOS("
            + "  COS(RADIANS(:lat)) * COS(RADIANS(s.latitude)) * "
            + "  COS(RADIANS(s.longitude) - RADIANS(:lng)) + "
            + "  SIN(RADIANS(:lat)) * SIN(RADIANS(s.latitude))"
            + ")) <= :maxDist",
        nativeQuery = true)
    @SuppressWarnings("checkstyle:ParameterNumber")
    Page<Event> findActiveByPeriodPagedWithinDistance(
            @Param("fromDt") LocalDateTime from,
            @Param("toDt") LocalDateTime to,
            @Param("startOfToday") LocalDateTime startOfToday,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("maxDist") double maxDistance,
            Pageable pageable);

    /**
     * Récupère tous les événements actifs non passés avec pagination,
     * filtrés par distance au saloon (formule de Haversine).
     */
    @Query(value = "SELECT e.* FROM event e "
            + "JOIN saloon s ON e.saloon_id = s.id "
            + "WHERE e.is_active = true "
            + "AND e.start_date_time >= :startOfToday "
            + "AND (6371000 * ACOS("
            + "  COS(RADIANS(:lat)) * COS(RADIANS(s.latitude)) * "
            + "  COS(RADIANS(s.longitude) - RADIANS(:lng)) + "
            + "  SIN(RADIANS(:lat)) * SIN(RADIANS(s.latitude))"
            + ")) <= :maxDist "
            + "ORDER BY e.start_date_time ASC",
        countQuery = "SELECT COUNT(*) FROM event e "
            + "JOIN saloon s ON e.saloon_id = s.id "
            + "WHERE e.is_active = true "
            + "AND e.start_date_time >= :startOfToday "
            + "AND (6371000 * ACOS("
            + "  COS(RADIANS(:lat)) * COS(RADIANS(s.latitude)) * "
            + "  COS(RADIANS(s.longitude) - RADIANS(:lng)) + "
            + "  SIN(RADIANS(:lat)) * SIN(RADIANS(s.latitude))"
            + ")) <= :maxDist",
        nativeQuery = true)
    Page<Event> findAllActiveNotPastPagedWithinDistance(
            @Param("startOfToday") LocalDateTime startOfToday,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("maxDist") double maxDistance,
            Pageable pageable);
}

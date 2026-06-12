package com.backend_project_template.domains.event;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventInterestRepository extends JpaRepository<EventInterest, Long> {

    Optional<EventInterest> findByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT COUNT(ei) FROM EventInterest ei WHERE ei.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);

    void deleteByUserIdAndEventId(Long userId, Long eventId);

    void deleteByUserId(Long userId);
}

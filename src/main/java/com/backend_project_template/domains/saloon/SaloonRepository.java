package com.backend_project_template.domains.saloon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("checkstyle:ParameterNumber")
public interface SaloonRepository extends JpaRepository<Saloon, Long> {
    Optional<Saloon> findByName(String name);

    @Query("SELECT s FROM Saloon s WHERE s.isActive = true "
            + "AND s.latitude BETWEEN :minLat AND :maxLat "
            + "AND s.longitude BETWEEN :minLng AND :maxLng")
    List<Saloon> findByBoundingBox(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng);

    List<Saloon> findByIsActiveTrue();

    long countByIsActiveTrue();
}

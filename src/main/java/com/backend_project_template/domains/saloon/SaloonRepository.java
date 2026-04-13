package com.backend_project_template.domains.saloon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        @Query("SELECT s FROM Saloon s WHERE s.isActive = true "
                        + "AND s.isPrivate = false "
                        + "AND s.latitude BETWEEN :minLat AND :maxLat "
                        + "AND s.longitude BETWEEN :minLng AND :maxLng")
        List<Saloon> findPublicByBoundingBox(
                        @Param("minLat") BigDecimal minLat,
                        @Param("maxLat") BigDecimal maxLat,
                        @Param("minLng") BigDecimal minLng,
                        @Param("maxLng") BigDecimal maxLng);

        @Query("SELECT s FROM Saloon s WHERE s.isActive = true AND s.type = :type "
                        + "AND s.latitude BETWEEN :minLat AND :maxLat "
                        + "AND s.longitude BETWEEN :minLng AND :maxLng")
        List<Saloon> findByBoundingBoxAndType(
                        @Param("minLat") BigDecimal minLat,
                        @Param("maxLat") BigDecimal maxLat,
                        @Param("minLng") BigDecimal minLng,
                        @Param("maxLng") BigDecimal maxLng,
                        @Param("type") SaloonType type);

        @Query("SELECT s FROM Saloon s WHERE s.isActive = true "
                        + "AND s.isPrivate = false AND s.type = :type "
                        + "AND s.latitude BETWEEN :minLat AND :maxLat "
                        + "AND s.longitude BETWEEN :minLng AND :maxLng")
        List<Saloon> findPublicByBoundingBoxAndType(
                        @Param("minLat") BigDecimal minLat,
                        @Param("maxLat") BigDecimal maxLat,
                        @Param("minLng") BigDecimal minLng,
                        @Param("maxLng") BigDecimal maxLng,
                        @Param("type") SaloonType type);

        List<Saloon> findByIsActiveTrue();

        List<Saloon> findByIsActiveTrueAndIsPrivateFalse();

        List<Saloon> findByIsActiveTrueAndType(SaloonType type);

        List<Saloon> findByIsActiveTrueAndIsPrivateFalseAndType(SaloonType type);

        long countByIsActiveTrue();

        /**
         * Récupère tous les saloons avec pagination
         */
        Page<Saloon> findAll(Pageable pageable);

        /**
         * Recherche des saloons par nom avec pagination
         */
        @Query("SELECT s FROM Saloon s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))")
        Page<Saloon> searchSaloons(@Param("search") String search, Pageable pageable);
}

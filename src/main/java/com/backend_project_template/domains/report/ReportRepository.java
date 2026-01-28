package com.backend_project_template.domains.report;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * Récupère tous les signalements triés par date de création décroissante.
     */
    List<Report> findAllByOrderByCreatedAtDesc();

    /**
     * Récupère les signalements par statut.
     */
    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    /**
     * Récupère les signalements faits par un utilisateur.
     */
    List<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    /**
     * Récupère les signalements contre un utilisateur.
     */
    List<Report> findByReportedIdOrderByCreatedAtDesc(Long reportedId);

    /**
     * Compte le nombre de signalements en attente.
     */
    long countByStatus(ReportStatus status);

    /**
     * Vérifie si un signalement existe déjà entre deux utilisateurs récemment.
     */
    @Query("SELECT COUNT(r) > 0 FROM Report r WHERE r.reporter.id = :reporterId AND r.reported.id = :reportedId AND r.createdAt > :since")
    boolean existsRecentReport(@Param("reporterId") Long reporterId, @Param("reportedId") Long reportedId,
            @Param("since") java.time.LocalDateTime since);

    /**
     * Supprime les signalements faits par un utilisateur.
     */
    void deleteByReporter(com.backend_project_template.domains.user.User reporter);

    /**
     * Supprime les signalements contre un utilisateur.
     */
    void deleteByReported(com.backend_project_template.domains.user.User reported);
}

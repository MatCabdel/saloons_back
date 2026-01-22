package com.backend_project_template.domains.report;

import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import com.backend_project_template.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    /** Délai en heures avant de pouvoir signaler à nouveau le même utilisateur. */
    private static final int REPORT_COOLDOWN_HOURS = 24;

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final SaloonRepository saloonRepository;
    private final ReportMapper reportMapper;

    public ReportService(
            ReportRepository reportRepository,
            UserRepository userRepository,
            SaloonRepository saloonRepository,
            ReportMapper reportMapper) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.saloonRepository = saloonRepository;
        this.reportMapper = reportMapper;
    }

    /**
     * Crée un nouveau signalement.
     */
    @Transactional
    public ReportDTO createReport(Long reporterId, CreateReportDTO createReportDTO) {
        // Vérifier qu'un signalement n'a pas déjà été fait récemment (dernières 24h)
        LocalDateTime since = LocalDateTime.now().minusHours(REPORT_COOLDOWN_HOURS);
        if (reportRepository.existsRecentReport(reporterId, createReportDTO.reportedId(), since)) {
            throw new IllegalStateException("Vous avez déjà signalé cet utilisateur récemment.");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        User reported = userRepository.findById(createReportDTO.reportedId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur signalé non trouvé"));

        if (reporterId.equals(createReportDTO.reportedId())) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous signaler vous-même.");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReason(createReportDTO.reason());
        report.setDescription(createReportDTO.description());
        report.setStatus(ReportStatus.PENDING);

        // Associer le saloon si fourni
        if (createReportDTO.saloonId() != null) {
            Saloon saloon = saloonRepository.findById(createReportDTO.saloonId())
                    .orElse(null);
            report.setSaloon(saloon);
        }

        Report savedReport = reportRepository.save(report);
        return reportMapper.toDTO(savedReport);
    }

    /**
     * Récupère tous les signalements (pour l'admin).
     */
    public List<ReportDTO> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les signalements par statut.
     */
    public List<ReportDTO> getReportsByStatus(ReportStatus status) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un signalement par son ID.
     */
    public ReportDTO getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Signalement non trouvé"));
        return reportMapper.toDTO(report);
    }

    /**
     * Met à jour le statut d'un signalement.
     */
    @Transactional
    public ReportDTO updateReportStatus(Long id, ReportStatus newStatus) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Signalement non trouvé"));

        report.setStatus(newStatus);
        Report updatedReport = reportRepository.save(report);
        return reportMapper.toDTO(updatedReport);
    }

    /**
     * Compte le nombre de signalements en attente.
     */
    public long countPendingReports() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }

    /**
     * Récupère les signalements d'un utilisateur spécifique (contre lui).
     */
    public List<ReportDTO> getReportsAgainstUser(Long userId) {
        return reportRepository.findByReportedIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }
}

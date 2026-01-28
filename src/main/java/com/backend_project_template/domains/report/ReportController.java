package com.backend_project_template.domains.report;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    public ReportController(ReportService reportService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    /**
     * Crée un nouveau signalement (utilisateur authentifié).
     */
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody CreateReportDTO createReportDTO, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User reporter = userRepository.findByEmail(principal.getName())
                .orElse(null);

        if (reporter == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ReportDTO report = reportService.createReport(reporter.getId(), createReportDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Récupère tous les signalements (admin).
     */
    @GetMapping("/admin")
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    /**
     * Récupère les signalements par statut (admin).
     */
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<List<ReportDTO>> getReportsByStatus(@PathVariable ReportStatus status) {
        return ResponseEntity.ok(reportService.getReportsByStatus(status));
    }

    /**
     * Récupère un signalement par ID (admin).
     */
    @GetMapping("/admin/{id}")
    public ResponseEntity<ReportDTO> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    /**
     * Met à jour le statut d'un signalement (admin).
     */
    @PatchMapping("/admin/{id}/status")
    public ResponseEntity<ReportDTO> updateReportStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        ReportStatus newStatus = ReportStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(reportService.updateReportStatus(id, newStatus));
    }

    /**
     * Compte le nombre de signalements en attente (admin).
     */
    @GetMapping("/admin/count/pending")
    public ResponseEntity<Map<String, Long>> countPendingReports() {
        return ResponseEntity.ok(Map.of("count", reportService.countPendingReports()));
    }

    /**
     * Récupère la liste des raisons de signalement disponibles.
     */
    @GetMapping("/reasons")
    public ResponseEntity<List<Map<String, String>>> getReportReasons() {
        List<Map<String, String>> reasons = java.util.Arrays.stream(ReportReason.values())
                .map(reason -> Map.of(
                        "value", reason.name(),
                        "label", reason.getDisplayName()))
                .toList();
        return ResponseEntity.ok(reasons);
    }
}

package com.backend_project_template.domains.report;

import java.time.LocalDateTime;

/**
 * DTO pour afficher un signalement dans l'admin.
 */
public record ReportDTO(
        Long id,
        ReporterDTO reporter,
        ReportedDTO reported,
        SaloonInfoDTO saloon,
        ReportReason reason,
        String reasonDisplayName,
        String description,
        ReportStatus status,
        String statusDisplayName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public record ReporterDTO(
            Long id,
            String userName,
            String imgUrl,
            LocalDateTime profileImageUpdatedAt,
            String city) {
    }

    public record ReportedDTO(
            Long id,
            String userName,
            String imgUrl,
            LocalDateTime profileImageUpdatedAt,
            String city) {
    }

    public record SaloonInfoDTO(
            Long id,
            String name,
            String city) {
    }
}

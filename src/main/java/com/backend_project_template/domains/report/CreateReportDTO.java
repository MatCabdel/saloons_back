package com.backend_project_template.domains.report;

/**
 * DTO pour créer un signalement.
 */
public record CreateReportDTO(
        Long reportedId,
        Long saloonId,
        ReportReason reason,
        String description) {
}

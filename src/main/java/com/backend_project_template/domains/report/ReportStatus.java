package com.backend_project_template.domains.report;

/**
 * Statuts d'un signalement.
 */
public enum ReportStatus {
    PENDING("En attente"),
    REVIEWED("Examiné"),
    RESOLVED("Résolu"),
    DISMISSED("Rejeté");

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

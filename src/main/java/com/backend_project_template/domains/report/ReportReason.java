package com.backend_project_template.domains.report;

/**
 * Raisons de signalement disponibles.
 */
public enum ReportReason {
    INAPPROPRIATE_BEHAVIOR("Comportement inapproprié"),
    UNWANTED_PHYSICAL_CONTACT("Interaction physique non voulue"),
    HARASSMENT("Harcèlement"),
    FAKE_PROFILE("Faux profil"),
    OTHER("Autre");

    private final String displayName;

    ReportReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

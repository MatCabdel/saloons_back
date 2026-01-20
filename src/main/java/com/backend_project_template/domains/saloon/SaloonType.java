package com.backend_project_template.domains.saloon;

/**
 * Types de saloons disponibles.
 */
public enum SaloonType {
    BAR("Bar"),
    DISCO("Discothèque"),
    PUBLIC("Lieu public"),
    SPORT("Salle de sport"),
    LOISIRS("Loisirs"),
    TRAVAIL("École / Fac");

    private final String displayName;

    SaloonType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

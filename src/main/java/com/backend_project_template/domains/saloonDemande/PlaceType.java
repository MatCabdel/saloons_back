package com.backend_project_template.domains.saloonDemande;

public enum PlaceType {
    BAR_RESTAURANT("Bar / Restaurant"),
    NIGHTCLUB("Discothèque"),
    PUBLIC_PLACE("Lieu public"),
    LEISURE("Loisirs"),
    WORK("Travail / Coworking");

    private final String label;

    PlaceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

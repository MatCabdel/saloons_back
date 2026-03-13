package com.backend_project_template.domains.contact;

public enum ContactSubject {
    QUESTION("Question générale"),
    SUGGESTION("Suggestion d'amélioration"),
    BUG("Signaler un bug"),
    PARTNERSHIP("Proposition de partenariat"),
    REPORT("Signalement"),
    OTHER("Autre");

    private final String label;

    ContactSubject(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

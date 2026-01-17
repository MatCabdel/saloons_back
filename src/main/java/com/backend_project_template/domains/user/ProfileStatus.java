package com.backend_project_template.domains.user;

/**
 * Enumeration representing the profile completion status of a user.
 */
public enum ProfileStatus {
    /**
     * User has registered but has not completed the profile onboarding.
     */
    PROFILE_INCOMPLETE,

    /**
     * User has completed the onboarding and has full access to the application.
     */
    ACTIVE
}

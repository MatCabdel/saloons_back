package com.backend_project_template.domains.analytics;

/**
 * Types d'événements analytiques trackés dans le système.
 * Utilisés pour alimenter les statistiques avancées.
 */
public enum AnalyticsEventType {
    USER_REGISTERED,
    USER_LOGIN,
    USER_ENTERED_SALOON,
    USER_EXITED_SALOON,
    USER_LIKED,
    USER_MATCHED,
    CONVERSATION_STARTED,
    MESSAGE_SENT,
    HEART_REQUEST_SENT,
    HEART_REQUEST_ACCEPTED,
    PREMIUM_PURCHASED,
    PREMIUM_CANCELLED,
    PROFILE_COMPLETED
}

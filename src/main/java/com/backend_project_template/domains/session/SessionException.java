package com.backend_project_template.domains.session;

/**
 * Exception personnalisée pour les erreurs de session.
 */
public class SessionException extends RuntimeException {

    public SessionException(String message) {
        super(message);
    }

    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }
}

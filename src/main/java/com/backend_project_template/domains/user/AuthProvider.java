package com.backend_project_template.domains.user;

/**
 * Enumeration representing the authentication provider used by a user.
 */
public enum AuthProvider {
    /**
     * User registered with email and password.
     */
    EMAIL,

    /**
     * User registered via Google Sign-In.
     */
    GOOGLE,

    /**
     * User registered via Facebook Login.
     */
    FACEBOOK,

    /**
     * User registered via Sign in with Apple.
     */
    APPLE
}

package com.backend_project_template.domains.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for Firebase authentication request.
 */
public class FirebaseAuthRequest {

    @NotBlank(message = "Firebase token is required")
    private String firebaseToken;

    public FirebaseAuthRequest() {
    }

    public FirebaseAuthRequest(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }
}

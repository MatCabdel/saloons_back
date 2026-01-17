package com.backend_project_template.domains.auth;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for verifying Firebase authentication tokens.
 */
@Service
public class FirebaseAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseAuthService.class);

    /**
     * Verifies a Firebase ID token and returns the decoded token.
     *
     * @param idToken the Firebase ID token to verify
     * @return FirebaseToken if valid, null otherwise
     */
    public FirebaseToken verifyToken(String idToken) {
        if (!isFirebaseInitialized()) {
            LOGGER.warn("Firebase not initialized - cannot verify token");
            return null;
        }

        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            LOGGER.error("Failed to verify Firebase token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the email from a Firebase token.
     *
     * @param token the Firebase token
     * @return the email address, or null if not available
     */
    public String getEmailFromToken(FirebaseToken token) {
        return token != null ? token.getEmail() : null;
    }

    /**
     * Extracts the Firebase UID from a token.
     *
     * @param token the Firebase token
     * @return the Firebase UID
     */
    public String getUidFromToken(FirebaseToken token) {
        return token != null ? token.getUid() : null;
    }

    /**
     * Extracts the display name from a Firebase token.
     *
     * @param token the Firebase token
     * @return the display name, or null if not available
     */
    public String getNameFromToken(FirebaseToken token) {
        return token != null ? token.getName() : null;
    }

    /**
     * Extracts the picture URL from a Firebase token.
     *
     * @param token the Firebase token
     * @return the picture URL, or null if not available
     */
    public String getPictureFromToken(FirebaseToken token) {
        return token != null ? token.getPicture() : null;
    }

    /**
     * Gets the sign-in provider from a Firebase token.
     *
     * @param token the Firebase token
     * @return the provider ID (e.g., "google.com", "facebook.com", "password")
     */
    public String getProviderFromToken(FirebaseToken token) {
        if (token == null || token.getClaims() == null) {
            return null;
        }
        Object signInProvider = token.getClaims().get("firebase");
        if (signInProvider instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> firebase = (java.util.Map<String, Object>) signInProvider;
            return (String) firebase.get("sign_in_provider");
        }
        return null;
    }

    private boolean isFirebaseInitialized() {
        return !FirebaseApp.getApps().isEmpty();
    }
}

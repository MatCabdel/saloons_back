package com.backend_project_template.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Firebase Admin SDK initialization.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.json:}")
    private String firebaseCredentialsJson;

    @Value("${firebase.credentials.file:}")
    private String firebaseCredentialsFile;

    /**
     * Initializes Firebase Admin SDK on application startup.
     */
    @PostConstruct
    public void initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                FirebaseOptions options = buildFirebaseOptions();
                if (options != null) {
                    FirebaseApp.initializeApp(options);
                    LOGGER.info("Firebase Admin SDK initialized successfully");
                } else {
                    LOGGER.warn("Firebase credentials not configured - Firebase auth disabled");
                }
            } catch (IOException e) {
                LOGGER.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            }
        }
    }

    private FirebaseOptions buildFirebaseOptions() throws IOException {
        GoogleCredentials credentials = null;

        // Try JSON string first (for environment variables)
        if (firebaseCredentialsJson != null && !firebaseCredentialsJson.isEmpty()) {
            InputStream stream = new ByteArrayInputStream(
                    firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
            credentials = GoogleCredentials.fromStream(stream);
            LOGGER.info("Firebase credentials loaded from JSON string");
        }
        // Then try file path
        else if (firebaseCredentialsFile != null && !firebaseCredentialsFile.isEmpty()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(firebaseCredentialsFile);
            if (stream != null) {
                credentials = GoogleCredentials.fromStream(stream);
                LOGGER.info("Firebase credentials loaded from file: {}", firebaseCredentialsFile);
            }
        }

        if (credentials == null) {
            return null;
        }

        return FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
    }
}

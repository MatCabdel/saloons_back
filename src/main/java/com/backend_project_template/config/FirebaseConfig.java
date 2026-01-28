package com.backend_project_template.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Value("${GOOGLE_APPLICATION_CREDENTIALS:}")
    private String googleApplicationCredentials;

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
                }
            } catch (IOException e) {
                LOGGER.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            }
        }
    }

    private FirebaseOptions buildFirebaseOptions() throws IOException {
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialsPath == null || credentialsPath.isBlank()) {
            credentialsPath = googleApplicationCredentials;
        }

        GoogleCredentials credentials;
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            credentials = loadCredentialsFromFile(credentialsPath);
        } else if (firebaseCredentialsFile != null && !firebaseCredentialsFile.isBlank()) {
            credentials = loadCredentialsFromFileOrClasspath(firebaseCredentialsFile);
        } else {
            throw new IllegalStateException(
                "Missing GOOGLE_APPLICATION_CREDENTIALS environment variable for Firebase Admin SDK");
        }

        return FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
    }

    private GoogleCredentials loadCredentialsFromFile(String path) throws IOException {
        Path credentialPath = Path.of(path);
        if (!Files.exists(credentialPath)) {
            throw new IllegalStateException("Firebase credentials file not found at: " + path);
        }
        try (InputStream stream = new FileInputStream(path)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            LOGGER.info("Firebase credentials loaded from file: {}", path);
            return credentials;
        }
    }

    private GoogleCredentials loadCredentialsFromFileOrClasspath(String pathOrResource) throws IOException {
        Path credentialPath = Path.of(pathOrResource);
        if (Files.exists(credentialPath)) {
            return loadCredentialsFromFile(pathOrResource);
        }
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(pathOrResource)) {
            if (stream == null) {
                throw new IllegalStateException("Firebase credentials not found at path or classpath: " + pathOrResource);
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            LOGGER.info("Firebase credentials loaded from classpath: {}", pathOrResource);
            return credentials;
        }
    }
}

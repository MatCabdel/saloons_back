package com.backend_project_template.domains.pushtoken;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service d'envoi de notifications push via Firebase Cloud Messaging (FCM).
 * Utilise l'API FCM HTTP v1 via le SDK Admin.
 */
@Service
public class FcmNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmNotificationService.class);

    private final PushTokenService pushTokenService;

    public FcmNotificationService(PushTokenService pushTokenService) {
        this.pushTokenService = pushTokenService;
    }

    /**
     * Vérifie si Firebase est initialisé
     */
    private boolean isFirebaseInitialized() {
        return !FirebaseApp.getApps().isEmpty();
    }

    /**
     * Envoie une notification à un utilisateur (tous ses appareils).
     * 
     * @param userId      L'ID de l'utilisateur destinataire
     * @param title       Le titre de la notification
     * @param body        Le corps de la notification
     * @param data        Données additionnelles (conversationId, messageId, etc.)
     */
    @Async
    public void sendToUser(Long userId, String title, String body, Map<String, String> data) {
        if (!isFirebaseInitialized()) {
            LOGGER.warn("🔔 Firebase not initialized, skipping push notification");
            return;
        }

        List<PushToken> tokens = pushTokenService.getActiveTokensForUser(userId);
        if (tokens.isEmpty()) {
            LOGGER.debug("🔔 No active push tokens for user {}", userId);
            return;
        }

        LOGGER.info("🔔 Sending notification to user {} ({} devices)", userId, tokens.size());

        List<String> tokenStrings = tokens.stream().map(PushToken::getToken).toList();
        sendToTokens(tokenStrings, title, body, data);
    }

    /**
     * Envoie une notification à plusieurs utilisateurs.
     * 
     * @param userIds     Liste des IDs utilisateurs
     * @param title       Le titre de la notification
     * @param body        Le corps de la notification
     * @param data        Données additionnelles
     */
    @Async
    public void sendToUsers(List<Long> userIds, String title, String body, Map<String, String> data) {
        if (!isFirebaseInitialized()) {
            LOGGER.warn("🔔 Firebase not initialized, skipping push notification");
            return;
        }

        List<PushToken> tokens = pushTokenService.getActiveTokensForUsers(userIds);
        if (tokens.isEmpty()) {
            LOGGER.debug("🔔 No active push tokens for users {}", userIds);
            return;
        }

        LOGGER.info("🔔 Sending notification to {} users ({} devices)", userIds.size(), tokens.size());

        List<String> tokenStrings = tokens.stream().map(PushToken::getToken).toList();
        sendToTokens(tokenStrings, title, body, data);
    }

    /**
     * Envoie une notification à une liste de tokens FCM.
     * Gère les erreurs et désactive les tokens invalides.
     */
    private void sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens.isEmpty()) {
            return;
        }

        try {
            // Construire la notification
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Configuration spécifique iOS (APNs)
            ApnsConfig apnsConfig = ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setSound("default")
                            .setBadge(1) // Badge simple V1 - toujours 1
                            .setContentAvailable(true)
                            .build())
                    .build();

            // Configuration spécifique Android
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setNotification(AndroidNotification.builder()
                            .setSound("default")
                            .setPriority(AndroidNotification.Priority.HIGH)
                            .build())
                    .build();

            // Si un seul token, utiliser send() simple
            if (tokens.size() == 1) {
                Message message = Message.builder()
                        .setToken(tokens.get(0))
                        .setNotification(notification)
                        .setApnsConfig(apnsConfig)
                        .setAndroidConfig(androidConfig)
                        .putAllData(data != null ? data : Map.of())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                LOGGER.info("🔔 Notification sent successfully: {}", response);
                return;
            }

            // Multi-tokens : utiliser sendEachForMulticast (anciennement sendMulticast)
            MulticastMessage multicastMessage = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(notification)
                    .setApnsConfig(apnsConfig)
                    .setAndroidConfig(androidConfig)
                    .putAllData(data != null ? data : Map.of())
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage);
            
            LOGGER.info("🔔 Multicast sent: {} success, {} failures", 
                    response.getSuccessCount(), response.getFailureCount());

            // Gérer les tokens invalides
            if (response.getFailureCount() > 0) {
                handleFailedTokens(tokens, response);
            }

        } catch (FirebaseMessagingException e) {
            LOGGER.error("🔔 Failed to send notification: {} - {}", e.getMessagingErrorCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("🔔 Unexpected error sending notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Gère les tokens qui ont échoué et désactive ceux qui sont invalides.
     */
    private void handleFailedTokens(List<String> tokens, BatchResponse response) {
        List<String> invalidTokens = new ArrayList<>();
        List<SendResponse> responses = response.getResponses();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (!sendResponse.isSuccessful()) {
                FirebaseMessagingException exception = sendResponse.getException();
                if (exception != null) {
                    MessagingErrorCode errorCode = exception.getMessagingErrorCode();
                    LOGGER.warn("🔔 Token {} failed: {}", i, errorCode);

                    // Ces codes indiquent un token invalide/expiré
                    if (errorCode == MessagingErrorCode.UNREGISTERED ||
                        errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                        invalidTokens.add(tokens.get(i));
                    }
                }
            }
        }

        // Désactiver les tokens invalides
        if (!invalidTokens.isEmpty()) {
            LOGGER.info("🔔 Deactivating {} invalid tokens", invalidTokens.size());
            pushTokenService.deactivateInvalidTokens(invalidTokens);
        }
    }
}

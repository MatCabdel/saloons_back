package com.backend_project_template.domains.pushtoken;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private static final int TOKEN_LOG_LENGTH = 30;

    private final PushTokenService pushTokenService;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    public FcmNotificationService(PushTokenService pushTokenService) {
        this.pushTokenService = pushTokenService;
    }

    /**
     * Vérifie si Firebase est initialisé et log le projectId
     */
    private boolean isFirebaseInitialized() {
        List<FirebaseApp> apps = FirebaseApp.getApps();
        if (apps.isEmpty()) {
            LOGGER.error("🔔 [push:firebase_initialized] initialized=FALSE - No Firebase app configured!");
            return false;
        }
        FirebaseApp app = apps.get(0);
        String projectId = app.getOptions().getProjectId();
        LOGGER.info("🔔 [push:firebase_initialized] initialized=TRUE, projectId={}, env={}",
                projectId != null ? projectId : "<not_set>", activeProfile);
        return true;
    }

    /**
     * Envoie une notification à un utilisateur (tous ses appareils).
     * 
     * @param userId L'ID de l'utilisateur destinataire
     * @param title  Le titre de la notification
     * @param body   Le corps de la notification
     * @param data   Données additionnelles (conversationId, messageId, etc.)
     */
    @Async
    public void sendToUser(Long userId, String title, String body, Map<String, String> data) {
        LOGGER.info("🔔 ==================== PUSH START ====================");
        LOGGER.info("🔔 [push:start] targetUserId={}, title='{}', env={}", userId, title, activeProfile);

        if (!isFirebaseInitialized()) {
            LOGGER.warn("🔔 [push:skip] reason=firebase_not_initialized");
            return;
        }

        List<PushToken> tokens = pushTokenService.getActiveTokensForUser(userId);
        if (tokens.isEmpty()) {
            LOGGER.warn("🔔 [push:skip] reason=no_active_tokens, userId={}", userId);
            LOGGER.info("🔔 ==================== PUSH END (no tokens) ====================");
            return;
        }

        // Log tous les tokens trouvés
        for (PushToken pt : tokens) {
            String tokenPreview = pt.getToken().substring(0, Math.min(TOKEN_LOG_LENGTH, pt.getToken().length()));
            LOGGER.info("🔔 [push:token_found] platform={}, token={}..., active={}",
                    pt.getPlatform(), tokenPreview, pt.getActive());
        }

        List<String> tokenStrings = tokens.stream().map(PushToken::getToken).toList();
        sendToTokens(tokenStrings, title, body, data);
        LOGGER.info("🔔 ==================== PUSH END ====================");
    }

    /**
     * Envoie une notification à plusieurs utilisateurs.
     * 
     * @param userIds Liste des IDs utilisateurs
     * @param title   Le titre de la notification
     * @param body    Le corps de la notification
     * @param data    Données additionnelles
     */
    @Async
    public void sendToUsers(List<Long> userIds, String title, String body, Map<String, String> data) {
        LOGGER.info("🔔 ==================== PUSH START (multi-user) ====================");
        LOGGER.info("🔔 [push:start] targetUserIds={}, title='{}', env={}", userIds, title, activeProfile);

        if (!isFirebaseInitialized()) {
            LOGGER.warn("🔔 [push:skip] reason=firebase_not_initialized");
            return;
        }

        List<PushToken> tokens = pushTokenService.getActiveTokensForUsers(userIds);
        if (tokens.isEmpty()) {
            LOGGER.warn("🔔 [push:skip] reason=no_active_tokens, userIds={}", userIds);
            LOGGER.info("🔔 ==================== PUSH END (no tokens) ====================");
            return;
        }

        // Log tous les tokens trouvés
        for (PushToken pt : tokens) {
            String tokenPreview = pt.getToken().substring(0, Math.min(TOKEN_LOG_LENGTH, pt.getToken().length()));
            LOGGER.info("🔔 [push:token_found] userId={}, platform={}, token={}..., active={}",
                    pt.getUser().getId(), pt.getPlatform(), tokenPreview, pt.getActive());
        }

        LOGGER.info("🔔 [push:sending] total_tokens={}", tokens.size());

        List<String> tokenStrings = tokens.stream().map(PushToken::getToken).toList();
        sendToTokens(tokenStrings, title, body, data);
        LOGGER.info("🔔 ==================== PUSH END ====================");
    }

    /**
     * Envoie une notification à une liste de tokens FCM.
     * Gère les erreurs et désactive les tokens invalides.
     * 
     * IMPORTANT iOS: La notification DOIT avoir un bloc "notification" (title/body)
     * pour s'afficher. Un payload "data-only" ne s'affiche PAS sur iOS.
     */
    private void sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens.isEmpty()) {
            LOGGER.warn("🔔 [push:skip] reason=empty_token_list");
            return;
        }

        LOGGER.info("🔔 [push:payload_type] type=notification+data (iOS-visible)");
        LOGGER.info("🔔 [push:payload] title='{}', body='{}', data={}", title, body, data);

        try {
            // Construire la notification (OBLIGATOIRE pour iOS!)
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Configuration spécifique iOS (APNs) - CRITIQUE pour affichage
            ApnsConfig apnsConfig = ApnsConfig.builder()
                    .putHeader("apns-priority", "10") // Haute priorité
                    .putHeader("apns-push-type", "alert") // Type alert (pas background)
                    .setAps(Aps.builder()
                            .setAlert(ApsAlert.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build())
                            .setSound("default")
                            .setBadge(1)
                            .setMutableContent(true) // Permet modification par extension
                            .build())
                    .build();

            // Configuration spécifique Android
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .setSound("default")
                            .setPriority(AndroidNotification.Priority.HIGH)
                            .build())
                    .build();

            // Si un seul token, utiliser send() simple
            if (tokens.size() == 1) {
                String tokenPreview = tokens.get(0).substring(0, Math.min(TOKEN_LOG_LENGTH, tokens.get(0).length()));
                LOGGER.info("🔔 [push:sending_single] token={}...", tokenPreview);

                Message message = Message.builder()
                        .setToken(tokens.get(0))
                        .setNotification(notification)
                        .setApnsConfig(apnsConfig)
                        .setAndroidConfig(androidConfig)
                        .putAllData(data != null ? data : Map.of())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                LOGGER.info("🔔 [push:sent_ok] messageId={}", response);
                return;
            }

            // Multi-tokens : utiliser sendEachForMulticast
            LOGGER.info("🔔 [push:sending_multicast] token_count={}", tokens.size());

            MulticastMessage multicastMessage = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(notification)
                    .setApnsConfig(apnsConfig)
                    .setAndroidConfig(androidConfig)
                    .putAllData(data != null ? data : Map.of())
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage);

            LOGGER.info("🔔 [push:sent_multicast] success={}, failures={}",
                    response.getSuccessCount(), response.getFailureCount());

            // Log les messageIds des succès
            for (int i = 0; i < response.getResponses().size(); i++) {
                SendResponse sr = response.getResponses().get(i);
                if (sr.isSuccessful()) {
                    LOGGER.info("🔔 [push:sent_ok] index={}, messageId={}", i, sr.getMessageId());
                }
            }

            // Gérer les tokens invalides
            if (response.getFailureCount() > 0) {
                handleFailedTokens(tokens, response);
            }

        } catch (FirebaseMessagingException e) {
            LOGGER.error("🔔 [push:sent_error] errorCode={}, message={}",
                    e.getMessagingErrorCode(), e.getMessage());
            LOGGER.error("🔔 [push:debug] Full exception:", e);
        } catch (Exception e) {
            LOGGER.error("🔔 [push:sent_error] unexpected_error={}", e.getMessage(), e);
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
                    String tokenPreview = tokens.get(i).substring(0,
                            Math.min(TOKEN_LOG_LENGTH, tokens.get(i).length()));
                    LOGGER.warn("🔔 [push:token_failed] index={}, token={}..., errorCode={}, message={}",
                            i, tokenPreview, errorCode, exception.getMessage());

                    // Ces codes indiquent un token invalide/expiré
                    if (errorCode == MessagingErrorCode.UNREGISTERED ||
                            errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                        invalidTokens.add(tokens.get(i));
                        LOGGER.info("🔔 [push:token_invalid] Marking for deactivation: {}...", tokenPreview);
                    }
                }
            }
        }

        // Désactiver les tokens invalides
        if (!invalidTokens.isEmpty()) {
            LOGGER.info("🔔 [push:cleanup] Deactivating {} invalid tokens", invalidTokens.size());
            pushTokenService.deactivateInvalidTokens(invalidTokens);
        }
    }
}

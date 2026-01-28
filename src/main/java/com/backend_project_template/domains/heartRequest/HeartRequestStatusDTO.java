package com.backend_project_template.domains.heartRequest;

import java.time.LocalDateTime;

/**
 * DTO pour retourner le statut du coup de cœur pour une conversation.
 */
public record HeartRequestStatusDTO(
        /** Si le user courant a envoyé un coup de cœur. */
        boolean sentByMe,
        /** Si l'autre user a envoyé un coup de cœur. */
        boolean receivedFromOther,
        /** Si la conversation est devenue permanente (coups de cœur mutuels). */
        boolean isPermanent,
        /** Si on peut encore envoyer un coup de cœur (dans les 12h). */
        boolean canSend,
        /** Date d'expiration de la fenêtre d'envoi (leftAt + 12h). */
        LocalDateTime expiresAt,
        /** Date à laquelle la conversation a expiré (leftAt). */
        LocalDateTime conversationExpiredAt) {
}

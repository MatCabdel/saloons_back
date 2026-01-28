package com.backend_project_template.domains.heartRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * DTO pour afficher les informations d'un coup de cœur.
 */
public record HeartRequestDTO(
        Long id,
        Long senderId,
        String senderUserName,
        Long receiverId,
        String receiverUserName,
        Long conversationId,
        Long saloonId,
        String saloonName,
        LocalDateTime createdAt,
        @JsonProperty("isMutual") boolean isMutual) {
}

package com.backend_project_template.domains.heartRequest;

/**
 * DTO pour envoyer un coup de cœur.
 */
public record SendHeartRequestDTO(
    Long conversationId,
    Long receiverId,
    Long saloonId
) {}

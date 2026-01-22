package com.backend_project_template.domains.conversation;

/**
 * DTO pour créer une nouvelle conversation.
 */
public record CreateConversationDTO(
    Long participantId,
    Long saloonId
) {}

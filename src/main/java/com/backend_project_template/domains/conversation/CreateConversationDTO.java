package com.backend_project_template.domains.conversation;

import java.time.LocalDateTime;

/**
 * DTO pour créer une nouvelle conversation.
 */
public record CreateConversationDTO(
    Long participantId,
    Long saloonId,
    LocalDateTime expiredAt
) {}

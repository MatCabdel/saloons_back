package com.backend_project_template.domains.saloonChat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Job de purge automatique des messages de chat public.
 * Exécuté toutes les heures, supprime les messages plus vieux que le TTL
 * configuré (par défaut 7 jours / 168h).
 */
@Component
public class SaloonChatPurgeJob {

    private static final Logger logger = LoggerFactory.getLogger(SaloonChatPurgeJob.class);

    @Value("${saloon.chat.ttl-hours:168}")
    private int ttlHours;

    @Autowired
    private SaloonMessageRepository messageRepository;

    @Scheduled(cron = "0 0 * * * *") // Toutes les heures
    @Transactional
    public void purgeOldMessages() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(ttlHours);
        int deleted = messageRepository.deleteMessagesOlderThan(cutoffDate);
        if (deleted > 0) {
            logger.info("Purged {} old chat messages (older than {} hours)", deleted, ttlHours);
        }
    }
}

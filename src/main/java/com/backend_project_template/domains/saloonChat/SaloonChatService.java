package com.backend_project_template.domains.saloonChat;

import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class SaloonChatService {

    private static final int MAX_MESSAGES = 100;
    private static final int DEFAULT_LIMIT = 50;

    @Autowired
    private SaloonMessageRepository messageRepository;

    @Autowired
    private SaloonRepository saloonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Récupère les messages d'un saloon (ancienne méthode, sans filtre par date)
     */
    public List<SaloonMessageDTO> getMessages(Long saloonId, int limit) {
        int effectiveLimit = Math.min(Math.max(1, limit), MAX_MESSAGES);
        List<SaloonMessage> messages = messageRepository.findBySaloonIdOrderByCreatedAtDesc(
                saloonId, PageRequest.of(0, effectiveLimit));
        Collections.reverse(messages); // Pour avoir les plus anciens en premier
        return messages.stream().map(SaloonMessageDTO::new).toList();
    }

    public List<SaloonMessageDTO> getMessages(Long saloonId) {
        return getMessages(saloonId, DEFAULT_LIMIT);
    }

    /**
     * Récupère les messages d'un saloon depuis une date donnée (joinedAt de
     * l'utilisateur)
     * C'est la méthode principale à utiliser pour afficher l'historique
     * personnalisé
     */
    public List<SaloonMessageDTO> getMessagesSince(Long saloonId, LocalDateTime since, int limit) {
        int effectiveLimit = Math.min(Math.max(1, limit), MAX_MESSAGES);
        List<SaloonMessage> messages = messageRepository.findBySaloonIdAndCreatedAtAfterOrderByCreatedAtAsc(
                saloonId, since, PageRequest.of(0, effectiveLimit));
        return messages.stream().map(SaloonMessageDTO::new).toList();
    }

    public List<SaloonMessageDTO> getMessagesSince(Long saloonId, LocalDateTime since) {
        return getMessagesSince(saloonId, since, DEFAULT_LIMIT);
    }

    @Transactional
    public SaloonMessageDTO sendMessage(Long saloonId, Long senderId, String content) {
        Saloon saloon = saloonRepository.findById(saloonId)
                .orElseThrow(() -> new RuntimeException("Saloon not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SaloonMessage message = new SaloonMessage(saloon, sender, content);
        message = messageRepository.save(message);

        SaloonMessageDTO dto = new SaloonMessageDTO(message);

        // Diffuser le message à tous les abonnés du saloon via WebSocket
        messagingTemplate.convertAndSend("/topic/saloon-chat/" + saloonId, dto);

        return dto;
    }

    @Transactional
    public void clearSaloonMessages(Long saloonId) {
        messageRepository.deleteBySaloonId(saloonId);
    }
}

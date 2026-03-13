package com.backend_project_template.domains.heartRequest;

import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.conversation.ConversationParticipant;
import com.backend_project_template.domains.conversation.ConversationRepository;
import com.backend_project_template.domains.pushtoken.FcmNotificationService;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import com.backend_project_template.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HeartRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartRequestService.class);

    /**
     * Durée de la fenêtre pour envoyer un coup de cœur après expiration (en
     * heures).
     */
    private static final int HEART_REQUEST_WINDOW_HOURS = 24;

    private final HeartRequestRepository heartRequestRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SaloonRepository saloonRepository;
    private final FcmNotificationService fcmNotificationService;

    public HeartRequestService(
            HeartRequestRepository heartRequestRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            SaloonRepository saloonRepository,
            FcmNotificationService fcmNotificationService) {
        this.heartRequestRepository = heartRequestRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.saloonRepository = saloonRepository;
        this.fcmNotificationService = fcmNotificationService;
    }

    /**
     * Envoie un coup de cœur à un autre utilisateur dans une conversation expirée.
     */
    @Transactional
    public HeartRequestDTO sendHeartRequest(Long senderId, Long conversationId, Long receiverId, Long saloonId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur expéditeur non trouvé"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur destinataire non trouvé"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation non trouvée"));

        // Vérifier que le sender ne s'envoie pas un coup de cœur à lui-même
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous envoyer un coup de cœur.");
        }

        // Sécurité : vérifier que le sender et le receiver sont bien participants de la
        // conversation
        if (conversation.getParticipant(senderId) == null) {
            throw new IllegalArgumentException("Vous n'êtes pas participant de cette conversation.");
        }
        if (conversation.getParticipant(receiverId) == null) {
            throw new IllegalArgumentException("Le destinataire n'est pas participant de cette conversation.");
        }

        // Vérifier que la conversation a expiré (un participant a quitté)
        LocalDateTime expiredAt = getConversationExpiredAt(conversation, senderId);
        if (expiredAt == null) {
            throw new IllegalStateException("La conversation n'est pas encore expirée.");
        }

        // Vérifier que nous sommes dans la fenêtre de temps
        LocalDateTime windowEnd = expiredAt.plusHours(HEART_REQUEST_WINDOW_HOURS);
        if (LocalDateTime.now().isAfter(windowEnd)) {
            throw new IllegalStateException("La fenêtre pour envoyer un coup de cœur est expirée.");
        }

        // Vérifier qu'un coup de cœur n'a pas déjà été envoyé
        if (heartRequestRepository.existsBySenderAndReceiverAndConversation(sender, receiver, conversation)) {
            throw new IllegalStateException("Vous avez déjà envoyé un coup de cœur pour cette conversation.");
        }

        // Récupérer le saloon si fourni
        Saloon saloon = null;
        if (saloonId != null) {
            saloon = saloonRepository.findById(saloonId).orElse(null);
        }

        // Créer le coup de cœur
        HeartRequest heartRequest = new HeartRequest();
        heartRequest.setSender(sender);
        heartRequest.setReceiver(receiver);
        heartRequest.setConversation(conversation);
        heartRequest.setSaloon(saloon);

        HeartRequest saved = heartRequestRepository.save(heartRequest);

        // Vérifier si c'est mutuel et rendre la conversation permanente
        boolean isMutual = checkAndMakeConversationPermanent(sender, receiver, conversation);

        return toDTO(saved, isMutual);
    }

    /**
     * Vérifie si les deux utilisateurs ont envoyé un coup de cœur et rend la
     * conversation permanente.
     * Envoie une notification push au premier expéditeur (MUTUAL_HEART).
     * 
     * @return true si le coup de cœur est mutuel, false sinon
     */
    @Transactional
    public boolean checkAndMakeConversationPermanent(User user1, User user2, Conversation conversation) {
        boolean user1SentToUser2 = heartRequestRepository
                .existsBySenderAndReceiverAndConversation(user1, user2, conversation);
        boolean user2SentToUser1 = heartRequestRepository
                .existsBySenderAndReceiverAndConversation(user2, user1, conversation);

        if (user1SentToUser2 && user2SentToUser1) {
            // Les deux ont envoyé un coup de cœur - rendre la conversation permanente
            if (!conversation.isPermanent()) {
                conversation.setPermanent(true);
                conversationRepository.save(conversation);

                // Réinitialiser les leftAt des participants pour que la conversation redevienne
                // active
                for (ConversationParticipant participant : conversation.getConversationParticipants()) {
                    participant.setLeftAt(null);
                }

                // Envoyer une notification au premier expéditeur (user2 avait envoyé en
                // premier, user1 vient de confirmer)
                sendMutualHeartNotification(user2, conversation);
            }
            return true;
        }
        return false;
    }

    /**
     * Envoie la notification push "coup de cœur partagé" au premier expéditeur.
     * Idempotent : la notification n'est envoyée que si la conversation vient de
     * passer en permanent (guard dans checkAndMakeConversationPermanent).
     */
    private void sendMutualHeartNotification(User firstSender, Conversation conversation) {
        LOGGER.info("💕 [mutual_heart_push] recipientId={}, conversationId={}",
                firstSender.getId(), conversation.getId());
        try {
            String title = "Saloons";
            String body = "Coup de cœur partagé \u2764\uFE0F La discussion continue\u00A0!";

            Map<String, String> data = new HashMap<>();
            data.put("type", "mutual_heart");
            data.put("conversationId", String.valueOf(conversation.getId()));

            fcmNotificationService.sendToUser(firstSender.getId(), title, body, data);
            LOGGER.info("💕 [mutual_heart_push_sent] recipientId={}", firstSender.getId());
        } catch (Exception e) {
            LOGGER.error("💕 [mutual_heart_push_error] recipientId={}, error={}",
                    firstSender.getId(), e.getMessage(), e);
        }
    }

    /**
     * Récupère le statut des coups de cœur pour une conversation et un utilisateur.
     */
    public HeartRequestStatusDTO getHeartRequestStatus(Long userId, Long conversationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation non trouvée"));

        // Sécurité : vérifier que l'utilisateur est participant de la conversation
        if (conversation.getParticipant(userId) == null) {
            throw new IllegalArgumentException("Vous n'êtes pas participant de cette conversation.");
        }

        // Trouver l'autre participant
        User otherUser = getOtherParticipant(conversation, userId);
        if (otherUser == null) {
            throw new IllegalStateException("Participant introuvable dans la conversation.");
        }

        // Vérifier si l'utilisateur a envoyé un coup de cœur
        boolean sentByMe = heartRequestRepository.existsBySenderAndReceiverAndConversation(user, otherUser,
                conversation);

        // Vérifier si l'autre utilisateur a envoyé un coup de cœur
        boolean receivedFromOther = heartRequestRepository
                .existsBySenderAndReceiverAndConversation(otherUser, user, conversation);

        // Vérifier si la conversation est permanente
        boolean isPermanent = conversation.isPermanent();

        // Calculer si l'utilisateur peut encore envoyer un coup de cœur
        LocalDateTime expiredAt = getConversationExpiredAt(conversation, userId);
        LocalDateTime expiresAt = null;
        boolean canSend = false;

        if (expiredAt != null) {
            expiresAt = expiredAt.plusHours(HEART_REQUEST_WINDOW_HOURS);
            canSend = !sentByMe && LocalDateTime.now().isBefore(expiresAt);
        }

        return new HeartRequestStatusDTO(sentByMe, receivedFromOther, isPermanent, canSend, expiresAt, expiredAt);
    }

    /**
     * Récupère les coups de cœur envoyés par un utilisateur.
     */
    public List<HeartRequestDTO> getSentHeartRequests(Long userId) {
        return heartRequestRepository.findBySenderId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les coups de cœur reçus par un utilisateur.
     */
    public List<HeartRequestDTO> getReceivedHeartRequests(Long userId) {
        return heartRequestRepository.findByReceiverId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retourne la date d'expiration de la conversation (premier leftAt trouvé).
     */
    private LocalDateTime getConversationExpiredAt(Conversation conversation, Long currentUserId) {
        // Retourner le premier leftAt trouvé (n'importe quel participant qui a quitté)
        return conversation.getConversationParticipants().stream()
                .filter(cp -> cp.getLeftAt() != null)
                .map(ConversationParticipant::getLeftAt)
                .findFirst()
                .orElse(null);
    }

    /**
     * Retourne l'autre participant de la conversation.
     */
    private User getOtherParticipant(Conversation conversation, Long currentUserId) {
        for (ConversationParticipant participant : conversation.getConversationParticipants()) {
            if (!participant.getUser().getId().equals(currentUserId)) {
                return participant.getUser();
            }
        }
        return null;
    }

    /**
     * Convertit une entité HeartRequest en DTO.
     */
    private HeartRequestDTO toDTO(HeartRequest heartRequest) {
        return toDTO(heartRequest, false);
    }

    /**
     * Convertit une entité HeartRequest en DTO avec info sur le match mutuel.
     */
    private HeartRequestDTO toDTO(HeartRequest heartRequest, boolean isMutual) {
        return new HeartRequestDTO(
                heartRequest.getId(),
                heartRequest.getSender().getId(),
                heartRequest.getSender().getUserName(),
                heartRequest.getReceiver().getId(),
                heartRequest.getReceiver().getUserName(),
                heartRequest.getConversation().getId(),
                heartRequest.getSaloon() != null ? heartRequest.getSaloon().getId() : null,
                heartRequest.getSaloon() != null ? heartRequest.getSaloon().getName() : null,
                heartRequest.getCreatedAt(),
                isMutual);
    }
}

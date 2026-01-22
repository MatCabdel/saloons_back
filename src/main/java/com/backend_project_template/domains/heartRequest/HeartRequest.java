package com.backend_project_template.domains.heartRequest;

import com.backend_project_template.domains.conversation.Conversation;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un "coup de cœur" envoyé par un utilisateur à un autre
 * après l'expiration d'une conversation éphémère.
 */
@Entity
@Table(name = "heart_requests", uniqueConstraints = @UniqueConstraint(columnNames = {
        "sender_id", "receiver_id", "conversation_id" }))
public class HeartRequest {

    /** Durée en heures pendant laquelle on peut envoyer un coup de cœur. */
    public static final int HEART_REQUEST_WINDOW_HOURS = 12;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** L'utilisateur qui envoie le coup de cœur. */
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonBackReference("heartSender")
    private User sender;

    /** L'utilisateur qui reçoit le coup de cœur. */
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    @JsonBackReference("heartReceiver")
    private User receiver;

    /** La conversation concernée. */
    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /** Le saloon où les utilisateurs se sont rencontrés. */
    @ManyToOne
    @JoinColumn(name = "saloon_id")
    private Saloon saloon;

    /** Date d'envoi du coup de cœur. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructeurs

    public HeartRequest() {
    }

    public HeartRequest(User sender, User receiver, Conversation conversation, Saloon saloon) {
        this.sender = sender;
        this.receiver = receiver;
        this.conversation = conversation;
        this.saloon = saloon;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public Saloon getSaloon() {
        return saloon;
    }

    public void setSaloon(Saloon saloon) {
        this.saloon = saloon;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

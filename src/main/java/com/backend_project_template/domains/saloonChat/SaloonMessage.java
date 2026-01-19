package com.backend_project_template.domains.saloonChat;

import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saloon_messages")
public class SaloonMessage {

    private static final int CONTENT_MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saloon_id", nullable = false)
    private Saloon saloon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = CONTENT_MAX_LENGTH)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructeurs
    public SaloonMessage() {
    }

    public SaloonMessage(Saloon saloon, User sender, String content) {
        this.saloon = saloon;
        this.sender = sender;
        this.content = content;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Saloon getSaloon() {
        return saloon;
    }

    public void setSaloon(Saloon saloon) {
        this.saloon = saloon;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

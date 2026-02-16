package com.backend_project_template.domains.pushtoken;

import com.backend_project_template.domains.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un token FCM (Firebase Cloud Messaging) d'un appareil.
 * Un utilisateur peut avoir plusieurs tokens (multi-devices).
 */
@Entity
@Table(name = "push_token", indexes = {
    @Index(name = "idx_push_token_token", columnList = "token", unique = true),
    @Index(name = "idx_push_token_user_id", columnList = "user_id")
})
public class PushToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Le token FCM (unique par appareil)
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * Plateforme de l'appareil (ios, android, web)
     */
    @Column(nullable = false, length = 20)
    private String platform;

    /**
     * L'utilisateur propriétaire de ce token
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Indique si le token est actif (false si FCM a signalé une erreur)
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Date de création/enregistrement du token
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière mise à jour
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructeurs

    public PushToken() {
    }

    public PushToken(String token, String platform, User user) {
        this.token = token;
        this.platform = platform;
        this.user = user;
        this.active = true;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

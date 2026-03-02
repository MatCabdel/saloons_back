package com.backend_project_template.domains.analytics;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Table d'événements analytiques pour le suivi détaillé.
 * Permet de reconstruire des funnels, calculer des rétentions fines,
 * et alimenter des statistiques avancées (V2).
 *
 * Pour le MVP, les statistiques sont calculées directement depuis les tables
 * existantes (User, Match, SaloonSession, etc.).
 */
@Entity
@Table(name = "analytics_events", indexes = {
    @Index(name = "idx_analytics_type", columnList = "eventType"),
    @Index(name = "idx_analytics_created", columnList = "createdAt"),
    @Index(name = "idx_analytics_user", columnList = "userId"),
    @Index(name = "idx_analytics_type_created", columnList = "eventType, createdAt"),
    @Index(name = "idx_analytics_user_type", columnList = "userId, eventType")
})
public class AnalyticsEvent {

    private static final int EVENT_TYPE_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = EVENT_TYPE_LENGTH)
    private AnalyticsEventType eventType;

    @Column(nullable = false)
    private Long userId;

    private Long saloonId;

    private String city;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Payload JSON optionnel pour données supplémentaires. */
    @Column(columnDefinition = "TEXT")
    private String payload;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public AnalyticsEvent() {
    }

    public AnalyticsEvent(AnalyticsEventType eventType, Long userId, Long saloonId, String city) {
        this.eventType = eventType;
        this.userId = userId;
        this.saloonId = saloonId;
        this.city = city;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AnalyticsEventType getEventType() {
        return eventType;
    }

    public void setEventType(AnalyticsEventType eventType) {
        this.eventType = eventType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSaloonId() {
        return saloonId;
    }

    public void setSaloonId(Long saloonId) {
        this.saloonId = saloonId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}

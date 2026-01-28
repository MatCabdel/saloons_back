package com.backend_project_template.domains.report;

import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloonSession.SaloonSession;
import com.backend_project_template.domains.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    /** Longueur maximale de la description. */
    private static final int DESCRIPTION_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** L'utilisateur qui signale. */
    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    @JsonBackReference("reporter")
    private User reporter;

    /** L'utilisateur signalé. */
    @ManyToOne
    @JoinColumn(name = "reported_id", nullable = false)
    @JsonBackReference("reported")
    private User reported;

    /** La session du saloon où l'incident s'est produit (optionnel). */
    @ManyToOne
    @JoinColumn(name = "saloon_session_id")
    private SaloonSession saloonSession;

    /** Le saloon où l'incident s'est produit. */
    @ManyToOne
    @JoinColumn(name = "saloon_id")
    private Saloon saloon;

    /** Raison du signalement. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    /** Description additionnelle (optionnel). */
    @Column(length = DESCRIPTION_MAX_LENGTH)
    private String description;

    /** Statut du signalement. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    /** Date de création du signalement. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Date de mise à jour du signalement. */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public User getReported() {
        return reported;
    }

    public void setReported(User reported) {
        this.reported = reported;
    }

    public SaloonSession getSaloonSession() {
        return saloonSession;
    }

    public void setSaloonSession(SaloonSession saloonSession) {
        this.saloonSession = saloonSession;
    }

    public Saloon getSaloon() {
        return saloon;
    }

    public void setSaloon(Saloon saloon) {
        this.saloon = saloon;
    }

    public ReportReason getReason() {
        return reason;
    }

    public void setReason(ReportReason reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
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

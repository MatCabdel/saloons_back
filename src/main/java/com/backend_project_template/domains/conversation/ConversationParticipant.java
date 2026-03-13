package com.backend_project_template.domains.conversation;

import com.backend_project_template.domains.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "conversation_participants")
public class ConversationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    /**
     * Date/heure du dernier message lu par ce participant.
     * Permet de calculer le nombre de messages non lus.
     */
    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    public boolean hasLeft() {
        return leftAt != null;
    }

    /**
     * Met à jour lastReadAt à maintenant.
     */
    public void markAsRead() {
        this.lastReadAt = LocalDateTime.now();
    }
}

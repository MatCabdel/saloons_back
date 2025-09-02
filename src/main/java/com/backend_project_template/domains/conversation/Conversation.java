package com.backend_project_template.domains.conversation;

import com.backend_project_template.Entity.User;
import com.backend_project_template.domains.message.Message;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Conversation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToMany
  @JoinTable(
    name = "conversation_participants",
    joinColumns = @JoinColumn(name = "conversation_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private List<User> participants;

  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
  private List<Message> messages;
}

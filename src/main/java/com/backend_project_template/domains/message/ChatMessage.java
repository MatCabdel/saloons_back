package com.backend_project_template.domains.message;

import com.backend_project_template.domains.conversation.Conversation;
import jakarta.persistence.*;
import java.awt.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String content;
  private String sender;
  private MessageType type;
  private LocalDateTime sentAt;

  @ManyToOne
  private Conversation conversation;
}

package com.backend_project_template.domains.match;

import com.backend_project_template.domains.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_matches", uniqueConstraints = @UniqueConstraint(columnNames = { "user1_id", "user2_id" }))
public class Match {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user1_id")
  private User user1;

  @ManyToOne
  @JoinColumn(name = "user2_id")
  private User user2;

  private LocalDateTime matchedAt;

  public Match() {}

  public Match(User user1, User user2) {
    this.user1 = user1;
    this.user2 = user2;
    this.matchedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser1() {
    return user1;
  }

  public void setUser1(User user1) {
    this.user1 = user1;
  }

  public User getUser2() {
    return user2;
  }

  public void setUser2(User user2) {
    this.user2 = user2;
  }

  public LocalDateTime getMatchedAt() {
    return matchedAt;
  }

  public void setMatchedAt(LocalDateTime matchedAt) {
    this.matchedAt = matchedAt;
  }
}

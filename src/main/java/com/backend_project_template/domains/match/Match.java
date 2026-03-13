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

  @Column(name = "left_by_user1_at")
  private LocalDateTime leftByUser1At;

  @Column(name = "left_by_user2_at")
  private LocalDateTime leftByUser2At;

  public Match() {
  }

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

  public LocalDateTime getLeftByUser1At() {
    return leftByUser1At;
  }

  public void setLeftByUser1At(LocalDateTime leftByUser1At) {
    this.leftByUser1At = leftByUser1At;
  }

  public LocalDateTime getLeftByUser2At() {
    return leftByUser2At;
  }

  public void setLeftByUser2At(LocalDateTime leftByUser2At) {
    this.leftByUser2At = leftByUser2At;
  }

  public boolean hasLeftForUser(User user) {
    if (user1.getId().equals(user.getId())) {
      return leftByUser1At != null;
    } else if (user2.getId().equals(user.getId())) {
      return leftByUser2At != null;
    }
    return false;
  }

  public boolean hasOtherUserLeft(User currentUser) {
    if (user1.getId().equals(currentUser.getId())) {
      return leftByUser2At != null;
    } else if (user2.getId().equals(currentUser.getId())) {
      return leftByUser1At != null;
    }
    return false;
  }

  public void markAsLeftBy(User user) {
    if (user1.getId().equals(user.getId())) {
      this.leftByUser1At = LocalDateTime.now();
    } else if (user2.getId().equals(user.getId())) {
      this.leftByUser2At = LocalDateTime.now();
    }
  }
}

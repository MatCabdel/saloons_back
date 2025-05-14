package com.backend_project_template.domains.saloonSession;

import com.backend_project_template.Entity.User;
import com.backend_project_template.domains.saloon.Saloon;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saloon_sessions")
public class SaloonSession {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  @JsonBackReference("user-session")
  private User user;

  @ManyToOne
  @JoinColumn(name = "saloon_id")
  @JsonBackReference
  private Saloon saloon;

  private LocalDateTime connectedAt;
  private LocalDateTime disconnectedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Saloon getSaloon() {
    return saloon;
  }

  public void setSaloon(Saloon saloon) {
    this.saloon = saloon;
  }

  public LocalDateTime getConnectedAt() {
    return connectedAt;
  }

  public void setConnectedAt(LocalDateTime connectedAt) {
    this.connectedAt = connectedAt;
  }

  public LocalDateTime getDisconnectedAt() {
    return disconnectedAt;
  }

  public void setDisconnectedAt(LocalDateTime disconnectedAt) {
    this.disconnectedAt = disconnectedAt;
  }
}

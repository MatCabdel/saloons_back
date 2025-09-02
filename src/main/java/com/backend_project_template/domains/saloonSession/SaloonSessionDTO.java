package com.backend_project_template.domains.saloonSession;

import java.time.LocalDateTime;

public class SaloonSessionDTO {

  private Long id;
  private Long userId;
  private Long saloonId;
  private LocalDateTime connectedAt;
  private LocalDateTime disconnectedAt;

  public SaloonSessionDTO() {}

  public SaloonSessionDTO(SaloonSession session) {
    this.id = session.getId();
    this.userId = session.getUser() != null ? session.getUser().getId() : null;
    this.saloonId = session.getSaloon() != null ? session.getSaloon().getId() : null;
    this.connectedAt = session.getConnectedAt();
    this.disconnectedAt = session.getDisconnectedAt();
  }

  // Getters & setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

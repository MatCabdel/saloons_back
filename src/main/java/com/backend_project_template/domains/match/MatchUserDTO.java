package com.backend_project_template.domains.match;

import com.backend_project_template.domains.user.User;
import java.time.LocalDateTime;

public class MatchUserDTO {

  private Long id;
  private String userName;
  private String imgUrl;
  private LocalDateTime matchedAt;
  private boolean sessionExpired;

  public MatchUserDTO() {
  }

  public MatchUserDTO(User user, LocalDateTime matchedAt, boolean sessionExpired) {
    this.id = user.getId();
    this.userName = user.getUserName();
    this.imgUrl = user.getImgUrl();
    this.matchedAt = matchedAt;
    this.sessionExpired = sessionExpired;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getImgUrl() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }

  public LocalDateTime getMatchedAt() {
    return matchedAt;
  }

  public void setMatchedAt(LocalDateTime matchedAt) {
    this.matchedAt = matchedAt;
  }

  public boolean isSessionExpired() {
    return sessionExpired;
  }

  public void setSessionExpired(boolean sessionExpired) {
    this.sessionExpired = sessionExpired;
  }
}

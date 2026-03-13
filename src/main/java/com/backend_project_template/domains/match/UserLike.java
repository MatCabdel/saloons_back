package com.backend_project_template.domains.match;

import com.backend_project_template.domains.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "user_likes", uniqueConstraints = @UniqueConstraint(columnNames = { "liker_id", "liked_id" }))
public class UserLike {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "liker_id")
  private User liker;

  @ManyToOne
  @JoinColumn(name = "liked_id")
  private User liked;

  public UserLike(User liker, User liked) {
    this.liker = liker;
    this.liked = liked;
  }

  public UserLike() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getLiker() {
    return liker;
  }

  public void setLiker(User liker) {
    this.liker = liker;
  }

  public User getLiked() {
    return liked;
  }

  public void setLiked(User liked) {
    this.liked = liked;
  }
}

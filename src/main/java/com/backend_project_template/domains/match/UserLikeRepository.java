package com.backend_project_template.domains.match;

import com.backend_project_template.domains.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLikeRepository extends JpaRepository<UserLike, Long> {
  boolean existsByLikerAndLiked(User liker, User liked);

  void deleteByLiker(User liker);

  void deleteByLiked(User liked);
}

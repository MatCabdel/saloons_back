package com.backend_project_template.repository;

import com.backend_project_template.Entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findById(Long id);

  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
}

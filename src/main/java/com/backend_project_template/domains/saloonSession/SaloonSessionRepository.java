package com.backend_project_template.domains.saloonSession;

import com.backend_project_template.domains.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaloonSessionRepository extends JpaRepository<SaloonSession, Long> {
  SaloonSession findFirstByUserIdAndDisconnectedAtIsNull(Long userId);

  void deleteByUser(User user);
}

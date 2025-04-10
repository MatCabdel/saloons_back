package com.backend_project_template.domains.saloonSession;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaloonSessionRepository extends JpaRepository<SaloonSession, Long> {}

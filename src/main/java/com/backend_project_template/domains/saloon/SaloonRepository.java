package com.backend_project_template.domains.saloon;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaloonRepository extends JpaRepository<Saloon, Long> {
  Optional<Saloon> findByName(String name);
}

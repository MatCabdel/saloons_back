package com.backend_project_template.domains.saloon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaloonRepository extends JpaRepository<com.backend_project_template.domains.saloon.Saloon, Long> {}

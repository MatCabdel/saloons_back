package com.backend_project_template.domains.saloonDemande;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaloonDemandeRepository extends JpaRepository<SaloonDemande, Long> {
}

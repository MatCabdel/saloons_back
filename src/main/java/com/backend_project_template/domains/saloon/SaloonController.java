package com.backend_project_template.domains.saloon;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/saloon")
public class SaloonController {

  private final SaloonRepository saloonRepository;

  public SaloonController(SaloonRepository saloonRepository) {
    this.saloonRepository = saloonRepository;
  }

  @GetMapping
  public ResponseEntity<List<Saloon>> getAllSaloons() {
    List<Saloon> saloons = saloonRepository.findAll();
    if (saloons.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(saloons);
  }
}

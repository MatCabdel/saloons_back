package com.backend_project_template.domains.saloonSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
public class SaloonSessionController {

  private final SaloonSessionRepository saloonSessionRepository;

  public SaloonSessionController(SaloonSessionRepository saloonSessionRepository) {
    this.saloonSessionRepository = saloonSessionRepository;
  }

  @GetMapping("/{userId}")
  public ResponseEntity<SaloonSessionDTO> getSaloonSession(@PathVariable Long userId) {
    SaloonSession session = saloonSessionRepository.findFirstByUserIdAndDisconnectedAtIsNull(userId);
    if (session == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(new SaloonSessionDTO(session));
  }
}

package com.backend_project_template.domains.saloon;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/saloon")
public class SaloonController {

  private final SaloonRepository saloonRepository;

  @Autowired
  private SaloonMapper saloonMapper;

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

  @GetMapping("/{id}")
  public ResponseEntity<SaloonDTO> getSaloonById(@PathVariable Long id) {
    return saloonRepository.findById(id).map(saloon -> ResponseEntity.ok(saloonMapper.toSaloonDTO(saloon))).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/users")
  public ResponseEntity<List<UserDTO>> getUsersInSaloon(@PathVariable Long id) {
    return saloonRepository
      .findById(id)
      .map(saloon -> {
        List<User> users = saloon.getUsersInSaloon();
        ResponseEntity<List<UserDTO>> response;
        if (users == null || users.isEmpty()) {
          response = ResponseEntity.noContent().build();
        } else {
          List<UserDTO> dtos = users
            .stream()
            .map(user -> {
              UserDTO dto = new UserDTO(user);
              dto.setAge(user.getBirthDate() != null ? java.time.Period.between(user.getBirthDate(), java.time.LocalDate.now()).getYears() : 0);
              return dto;
            })
            .toList();
          response = ResponseEntity.ok(dtos);
        }
        return response;
      })
      .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSaloon(@PathVariable Long id) {
    if (!saloonRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    saloonRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}

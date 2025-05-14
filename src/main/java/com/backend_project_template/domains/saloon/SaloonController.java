package com.backend_project_template.domains.saloon;

import java.util.List;

import com.backend_project_template.Entity.User;
import com.backend_project_template.domains.user.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  @GetMapping("/{id}")
  public ResponseEntity<Saloon> getSaloonById(@PathVariable Long id) {
    return saloonRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

    @GetMapping("/{id}/users")
    public ResponseEntity<List<UserDTO>> getUsersInSaloon(@PathVariable Long id) {
        return saloonRepository.findById(id)
                .map(saloon -> {
                    List<User> users = saloon.getUsersInSaloon();
                    ResponseEntity<List<UserDTO>> response;
                    if (users == null || users.isEmpty()) {
                        response = ResponseEntity.noContent().build();
                    } else {
                        List<UserDTO> dtos = users.stream().map(user -> {
                            UserDTO dto = new UserDTO(user);
                            dto.setAge(user.getBirthDate() != null
                                    ? java.time.Period.between(user.getBirthDate(), java.time.LocalDate.now()).getYears()
                                    : 0);
                            return dto;
                        }).toList();
                        response = ResponseEntity.ok(dtos);
                    }
                    return response;
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

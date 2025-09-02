package com.backend_project_template.domains.saloon;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SaloonService {

  private final SaloonRepository saloonRepository;

  public SaloonService(SaloonRepository saloonRepository) {
    this.saloonRepository = saloonRepository;
  }

  public List<Saloon> getAllSaloons() {
    List<Saloon> saloons = saloonRepository.findAll();
    return saloons.stream().collect(Collectors.toList());
  }
}

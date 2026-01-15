package com.backend_project_template.domains.saloon;

import com.backend_project_template.domains.user.UserDTO;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SaloonMapper {

  public SaloonDTO toSaloonDTO(Saloon saloon) {
    SaloonDTO saloonDTO = new SaloonDTO();
    saloonDTO.setId(saloon.getId());
    saloonDTO.setName(saloon.getName());
    saloonDTO.setImgUrl(saloon.getImgUrl());
    saloonDTO.setVisitorNumber(saloon.getVisitorNumber());
    saloonDTO.setLatitude(saloon.getLatitude());
    saloonDTO.setLongitude(saloon.getLongitude());
    saloonDTO.setCreatedAt(saloon.getCreatedAt());
    saloonDTO.setAddress(saloon.getAddress());
    saloonDTO.setCity(saloon.getCity());
    saloonDTO.setRadiusMeters(saloon.getRadiusMeters());
    saloonDTO.setIsActive(saloon.getIsActive());

    if (saloon.getUsersInSaloon() != null) {
      saloonDTO.setUsersInSaloon(saloon.getUsersInSaloon().stream().map(UserDTO::new).collect(Collectors.toList()));
    }

    return saloonDTO;
  }

  public Saloon toEntity(SaloonDTO dto) {
    Saloon saloon = new Saloon();
    saloon.setId(dto.getId());
    saloon.setName(dto.getName());
    saloon.setImgUrl(dto.getImgUrl());
    saloon.setVisitorNumber(dto.getVisitorNumber());
    saloon.setLatitude(dto.getLatitude());
    saloon.setLongitude(dto.getLongitude());
    saloon.setCreatedAt(dto.getCreatedAt());
    saloon.setAddress(dto.getAddress());
    return saloon;
  }
}

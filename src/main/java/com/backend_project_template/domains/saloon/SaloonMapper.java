package com.backend_project_template.domains.saloon;

import com.backend_project_template.domains.presence.SaloonMapDTO;
import org.springframework.stereotype.Component;

@Component
public class SaloonMapper {

  /**
   * Convertit une entité Saloon en SaloonDTO (sans charger les usersInSaloon pour
   * éviter N+1).
   * Utilisez cette méthode pour les listes de saloons.
   */
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
    saloonDTO.setIsPrivate(saloon.getIsPrivate());
    saloonDTO.setType(saloon.getType());

    // Note: Ne PAS charger usersInSaloon ici car cela crée un N+1 query.
    // Le connectedCount est récupéré via Redis dans le controller.

    return saloonDTO;
  }

  /**
   * Convertit une entité Saloon en SaloonMapDTO léger pour l'affichage carte.
   */
  public SaloonMapDTO toSaloonMapDTO(Saloon saloon, int connectedCount) {
    return new SaloonMapDTO(saloon, null, connectedCount);
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
    saloon.setType(dto.getType());
    saloon.setIsPrivate(dto.getIsPrivate());
    return saloon;
  }
}

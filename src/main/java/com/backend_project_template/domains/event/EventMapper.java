package com.backend_project_template.domains.event;

import com.backend_project_template.domains.saloon.Saloon;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    /**
     * Convertit une entité Event en EventDTO.
     * Inclut les informations du saloon associé et les données d'intérêt.
     */
    public EventDTO toEventDTO(Event event, long interestedCount, boolean isInterested) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setSubTitle(event.getSubTitle());
        dto.setImageUrl(event.getImageUrl());
        dto.setDescription(event.getDescription());
        dto.setStartDateTime(event.getStartDateTime());
        dto.setEndDateTime(event.getEndDateTime());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        dto.setIsActive(event.getIsActive());
        dto.setInterestedCount(interestedCount);
        dto.setIsInterested(isInterested);

        Saloon saloon = event.getSaloon();
        if (saloon != null) {
            dto.setSaloonId(saloon.getId());
            dto.setSaloonName(saloon.getName());
            dto.setSaloonImgUrl(saloon.getImgUrl());
            dto.setSaloonAddress(saloon.getAddress());
            dto.setSaloonCity(saloon.getCity());
            dto.setSaloonLatitude(
                    saloon.getLatitude() != null ? saloon.getLatitude().doubleValue() : null);
            dto.setSaloonLongitude(
                    saloon.getLongitude() != null ? saloon.getLongitude().doubleValue() : null);
            dto.setSaloonRadiusMeters(saloon.getRadiusMeters());
            dto.setSaloonType(saloon.getType() != null ? saloon.getType().name() : null);
            dto.setSaloonIsPrivate(saloon.getIsPrivate());
        }

        return dto;
    }
}

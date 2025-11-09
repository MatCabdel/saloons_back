package com.backend_project_template.domains.user;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {
        public UserDTO toUserDTO(User user) {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setUserName(user.getUserName());
            dto.setImgUrl(user.getImgUrl());
            dto.setCurrentSaloonId(user.getCurrentSaloon() != null ? user.getCurrentSaloon().getId() : null);
            return dto;
        }
}

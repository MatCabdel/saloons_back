package com.backend_project_template.domains.user;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserDTO toUserDTO(User user) {
    return new UserDTO(user);
  }
}

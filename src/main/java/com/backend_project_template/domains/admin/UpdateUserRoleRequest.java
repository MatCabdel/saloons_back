package com.backend_project_template.domains.admin;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserRoleRequest {

  @NotBlank
  private String role;

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}

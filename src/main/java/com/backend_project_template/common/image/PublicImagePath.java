package com.backend_project_template.common.image;

public enum PublicImagePath {
  USER_UPLOAD("/user/upload/"),
  UPLOADS_IMAGES("/uploads/images/");

  private final String pathPrefix;

  PublicImagePath(String pathPrefix) {
    this.pathPrefix = pathPrefix;
  }

  public String getPathPrefix() {
    return pathPrefix;
  }
}

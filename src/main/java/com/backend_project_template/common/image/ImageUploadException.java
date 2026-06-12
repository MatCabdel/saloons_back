package com.backend_project_template.common.image;

public class ImageUploadException extends RuntimeException {

  public ImageUploadException(String message) {
    super(message);
  }

  public ImageUploadException(String message, Throwable cause) {
    super(message, cause);
  }
}

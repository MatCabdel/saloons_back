package com.backend_project_template.common.image;

public record StoredImage(
    String filename,
    String publicUrl,
    String contentType,
    long sizeBytes) {
}

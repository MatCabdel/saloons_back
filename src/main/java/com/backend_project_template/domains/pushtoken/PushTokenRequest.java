package com.backend_project_template.domains.pushtoken;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO pour enregistrer un token push
 */
public record PushTokenRequest(
    @NotBlank(message = "Le token est obligatoire")
    String token,

    @NotBlank(message = "La plateforme est obligatoire")
    @Pattern(regexp = "^(ios|android|web)$", message = "La plateforme doit être 'ios', 'android' ou 'web'")
    String platform
) {}

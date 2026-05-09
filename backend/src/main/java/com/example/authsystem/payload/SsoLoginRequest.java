package com.example.authsystem.payload;

import jakarta.validation.constraints.NotBlank;

public record SsoLoginRequest(
    @NotBlank String username,
    @NotBlank String service
) {
}

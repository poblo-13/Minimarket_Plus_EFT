package com.minimarket.security.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Public registration payload. Roles are deliberately not accepted. */
public record RegisterRequest(
        @NotBlank(message = "username is required")
        @Size(min = 3, max = 100, message = "username must be between 3 and 100 characters")
        String username,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 128, message = "password must be between 8 and 128 characters")
        @Pattern(regexp = ".*[a-z].*", message = "password must contain a lowercase letter")
        @Pattern(regexp = ".*[A-Z].*", message = "password must contain an uppercase letter")
        @Pattern(regexp = ".*\\d.*", message = "password must contain a digit")
        String password) {
}

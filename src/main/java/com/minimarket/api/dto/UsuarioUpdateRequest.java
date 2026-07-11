package com.minimarket.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UsuarioUpdateRequest(
        @NotBlank @Size(min = 3, max = 100) String username,
        @Size(min = 8, max = 128)
        @Pattern(regexp = ".*[a-z].*", message = "password must contain a lowercase letter")
        @Pattern(regexp = ".*[A-Z].*", message = "password must contain an uppercase letter")
        @Pattern(regexp = ".*\\d.*", message = "password must contain a digit") String password,
        @Size(min = 1) Set<@Positive Long> rolIds) { }

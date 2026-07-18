package com.minimarket.security.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "username is required")
    @Size(max = 100, message = "username must not exceed 100 characters")
    private String username;

    @NotBlank(message = "password is required")
    @Size(max = 128, message = "password must not exceed 128 characters")
    private String password;
}

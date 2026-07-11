package com.minimarket.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(@NotBlank @Size(max = 100) String nombre) { }

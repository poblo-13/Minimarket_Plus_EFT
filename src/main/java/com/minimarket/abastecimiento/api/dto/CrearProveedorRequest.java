package com.minimarket.abastecimiento.api.dto;
import jakarta.validation.constraints.NotBlank;
public record CrearProveedorRequest(@NotBlank String nombre) { }

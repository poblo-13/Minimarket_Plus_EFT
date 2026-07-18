package com.minimarket.sucursal.api.dto;
import jakarta.validation.constraints.NotBlank;
public record CrearSucursalRequest(@NotBlank String nombre) { }

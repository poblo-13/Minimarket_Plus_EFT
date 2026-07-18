package com.minimarket.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Cantidad absoluta para un producto identificado en la URL. */
public record CarritoRequest(@NotNull @Min(1) Integer cantidad) { }

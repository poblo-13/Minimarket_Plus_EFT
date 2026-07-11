package com.minimarket.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record VentaRequest(@NotNull @Positive Long usuarioId, @NotNull @PastOrPresent LocalDateTime fecha) { }

package com.minimarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Público")
public class HolaMundoController {
    @GetMapping("/public/hola")
    @Operation(summary = "Saludo público", description = "Endpoint público; no requiere autenticación.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Saludo devuelto", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))))
    public String holaMundo() {
        return "¡Hola Mundo!";
    }
}

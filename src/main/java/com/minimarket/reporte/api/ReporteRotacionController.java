package com.minimarket.reporte.api;

import com.minimarket.reporte.RotacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController @Validated @RequestMapping(value = "/api/reportes/rotacion", produces = MediaType.APPLICATION_JSON_VALUE) @Tag(name = "Reportes") @SecurityRequirement(name = "bearerAuth")
public class ReporteRotacionController {
    private final RotacionService service; public ReporteRotacionController(RotacionService service) { this.service = service; }
    @GetMapping @Operation(summary = "Consulta rotación por producto")
    @ApiResponses({@ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = RotacionProductoResponse.class)))),
                   @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))), @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))})
    public List<RotacionProductoResponse> consultar(@RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                    @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                    @RequestParam(required = false) Long sucursalId) { return service.consultar(desde, hasta, sucursalId); }
}

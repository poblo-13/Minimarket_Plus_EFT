package com.minimarket.promocion.api;

import com.minimarket.promocion.PromocionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping(value = "/api/promociones", produces = MediaType.APPLICATION_JSON_VALUE) @Tag(name = "Promociones") @SecurityRequirement(name = "bearerAuth")
public class PromocionController {
    private final PromocionService service; public PromocionController(PromocionService service) { this.service = service; }
    @GetMapping @Operation(summary = "Lista promociones") @ApiResponses({@ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PromocionResponse.class)))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))}) public List<PromocionResponse> listar() { return service.listar(); }
    @GetMapping("/{id}") @Operation(summary = "Obtiene una promoción") @ApiResponses({@ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PromocionResponse.class))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))), @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))}) public PromocionResponse obtener(@PathVariable Long id) { return service.obtener(id); }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE) @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({@ApiResponse(responseCode = "201", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PromocionResponse.class))), @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))), @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))})
    public ResponseEntity<PromocionResponse> crear(@Valid @RequestBody PromocionRequest request) { PromocionResponse r = service.crear(request); return ResponseEntity.created(URI.create("/api/promociones/" + r.id())).body(r); }
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE) @Operation(requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PromocionRequest.class)))) @ApiResponses({@ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PromocionResponse.class))), @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))), @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))), @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))}) public PromocionResponse actualizar(@PathVariable Long id, @Valid @RequestBody PromocionRequest request) { return service.actualizar(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @ApiResponses({@ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))), @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))), @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))}) public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}

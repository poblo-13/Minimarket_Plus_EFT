package com.minimarket.promocion.api;

import com.minimarket.promocion.PromocionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping(value = "/api/promociones", produces = MediaType.APPLICATION_JSON_VALUE) @Tag(name = "Promociones")
public class PromocionController {
    private final PromocionService service; public PromocionController(PromocionService service) { this.service = service; }
    @GetMapping @Operation(summary = "Lista promociones") public List<PromocionResponse> listar() { return service.listar(); }
    @GetMapping("/{id}") @Operation(summary = "Obtiene una promoción") public PromocionResponse obtener(@PathVariable Long id) { return service.obtener(id); }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE) @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(@ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))))
    public ResponseEntity<PromocionResponse> crear(@Valid @RequestBody PromocionRequest request) { PromocionResponse r = service.crear(request); return ResponseEntity.created(URI.create("/api/promociones/" + r.id())).body(r); }
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE) public PromocionResponse actualizar(@PathVariable Long id, @Valid @RequestBody PromocionRequest request) { return service.actualizar(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}

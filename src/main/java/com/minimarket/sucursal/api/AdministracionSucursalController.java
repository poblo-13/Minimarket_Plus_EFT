package com.minimarket.sucursal.api;

import com.minimarket.sucursal.AdministracionStockService;
import com.minimarket.sucursal.StockSucursalService;
import com.minimarket.sucursal.SucursalRepository;
import com.minimarket.sucursal.api.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sucursales")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración de sucursales")
@SecurityRequirement(name = "bearerAuth")
public class AdministracionSucursalController {
    private final AdministracionStockService administracion;
    private final SucursalRepository sucursales;
    private final StockSucursalService stocks;
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear sucursal", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CrearSucursalRequest.class))))
    @ApiResponses({@ApiResponse(responseCode = "201", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = SucursalResponse.class))), @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public EntityModel<SucursalResponse> crear(@Valid @RequestBody CrearSucursalRequest request) {
        var sucursal = administracion.crearSucursal(request.nombre());
        return EntityModel.of(new SucursalResponse(sucursal.getId(), sucursal.getNombre()), linkTo(methodOn(AdministracionSucursalController.class).obtener(sucursal.getId())).withSelfRel());
    }
    @GetMapping("/{id}") @Operation(summary = "Obtener sucursal") @ApiResponses({@ApiResponse(responseCode = "200", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = SucursalResponse.class))), @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))}) public EntityModel<SucursalResponse> obtener(@PathVariable @Positive Long id) {
        var s = sucursales.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Sucursal no encontrada"));
        return EntityModel.of(new SucursalResponse(s.getId(), s.getNombre()), linkTo(methodOn(AdministracionSucursalController.class).obtener(id)).withSelfRel());
    }
    @PutMapping("/{id}/stock") @Operation(requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ConfigurarStockRequest.class)))) @ApiResponses({@ApiResponse(responseCode = "200", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = StockSucursalResponse.class))), @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))}) public EntityModel<StockSucursalResponse> configurar(@PathVariable @Positive Long id, @Valid @RequestBody ConfigurarStockRequest r) { return model(administracion.configurarStock(id, r.productoId(), r.disponible(), r.minimo(), r.proveedorId())); }
    @PostMapping("/{id}/entradas") @Operation(requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovimientoStockRequest.class)))) @ApiResponses({@ApiResponse(responseCode = "200", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = StockSucursalResponse.class))), @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))}) public EntityModel<StockSucursalResponse> entrada(@PathVariable @Positive Long id, @Valid @RequestBody MovimientoStockRequest r) { return model(administracion.entrada(id, r.productoId(), r.cantidad())); }
    @PostMapping("/{id}/salidas") @Operation(requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovimientoStockRequest.class)))) @ApiResponses({@ApiResponse(responseCode = "200", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = StockSucursalResponse.class))), @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))}) public EntityModel<StockSucursalResponse> salida(@PathVariable @Positive Long id, @Valid @RequestBody MovimientoStockRequest r) { return model(administracion.salida(id, r.productoId(), r.cantidad())); }
    private EntityModel<StockSucursalResponse> model(com.minimarket.sucursal.StockSucursal s) { return EntityModel.of(new StockSucursalResponse(s.getSucursal().getId(), s.getProducto().getId(), s.getDisponible(), s.getStockMinimo())); }
}
